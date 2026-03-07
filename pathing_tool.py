#!/usr/bin/env python3
import argparse
import json
import re
from pathlib import Path
from typing import Dict, List, Optional, Tuple

FIELD_WIDTH = 144.0


def strip_comments(text: str) -> str:
    text = re.sub(r"/\*.*?\*/", "", text, flags=re.S)
    text = re.sub(r"//.*", "", text)
    return text


def find_block_after(code: str, marker_regex: str) -> Optional[str]:
    match = re.search(marker_regex, code, flags=re.S)
    if not match:
        return None

    start = code.find("{", match.end() - 1)
    if start == -1:
        return None

    depth = 0
    for i in range(start, len(code)):
        ch = code[i]
        if ch == "{":
            depth += 1
        elif ch == "}":
            depth -= 1
            if depth == 0:
                return code[start + 1 : i]
    return None


def parse_pose_constants(code: str) -> Dict[str, Dict[str, float]]:
    pose_pattern = re.compile(
        r"public\s+static\s+final\s+Pose\s+(\w+)\s*=\s*new\s+Pose\(\s*([-\d.]+)\s*,\s*([-\d.]+)\s*,\s*Math\.toRadians\(([-\d.]+)\)\s*\)\s*;"
    )
    poses: Dict[str, Dict[str, float]] = {}
    for name, x, y, deg in pose_pattern.findall(code):
        poses[name] = {"x": float(x), "y": float(y), "deg": float(deg)}
    return poses


def mirror_pose(pose: Dict[str, float]) -> Dict[str, float]:
    return {
        "x": FIELD_WIDTH - pose["x"],
        "y": pose["y"],
        "deg": (180.0 - pose["deg"]) % 360.0,
    }


def require_pose(poses: Dict[str, Dict[str, float]], name: str) -> Dict[str, float]:
    if name not in poses:
        raise SystemExit(f"Missing required Pose constant: {name}")
    return poses[name]


def resolve_runtime_pose_map(
    poses: Dict[str, Dict[str, float]],
    alliance: str,
    location: str,
    force_close_score1: bool,
    start_close_skip_far: bool,
) -> Dict[str, Dict[str, float]]:
    blue = alliance.lower() == "blue"
    close = location.lower() == "close"

    start_close = require_pose(poses, "startPoseCloseBlue")
    start_far = require_pose(poses, "startPoseFarBlue")
    score_close = require_pose(poses, "scorePoseCloseBlue")
    score_far = require_pose(poses, "scorePoseFarBlue")

    if blue:
        mapping = {
            "intakeAlign1": require_pose(poses, "intakeAlign1Blue"),
            "intake1": require_pose(poses, "intake1Blue"),
            "intakeAlign2": require_pose(poses, "intakeAlign2Blue"),
            "intake2": require_pose(poses, "intake2Blue"),
            "intakeAlign3": require_pose(poses, "intakeAlign3Blue"),
            "intake3": require_pose(poses, "intake3Blue"),
            "targetExitPos": require_pose(poses, "targetExitPosCloseBlue") if (close and start_close_skip_far) else require_pose(poses, "targetExitPosFarBlue"),
            "intakeAlignPlayer": poses.get("IntakeAlignPlayerBlue", require_pose(poses, "intakeAlign1Blue")),
            "intakePlayer": poses.get("IntakePlayerBlue", require_pose(poses, "intake1Blue")),
            "startPose": start_close if close else start_far,
            "scorePoseGeneral": score_close if close else score_far,
        }
        mapping["scorePose1"] = score_close if force_close_score1 else mapping["scorePoseGeneral"]
        return mapping

    mapping = {
        "intakeAlign1": mirror_pose(require_pose(poses, "intakeAlign1Blue")),
        "intake1": mirror_pose(require_pose(poses, "intake1Blue")),
        "intakeAlign2": mirror_pose(require_pose(poses, "intakeAlign2Blue")),
        "intake2": mirror_pose(require_pose(poses, "intake2Blue")),
        "intakeAlign3": mirror_pose(require_pose(poses, "intakeAlign3Blue")),
        "intake3": mirror_pose(require_pose(poses, "intake3Blue")),
        "targetExitPos": mirror_pose(require_pose(poses, "targetExitPosCloseBlue")) if (close and start_close_skip_far) else mirror_pose(require_pose(poses, "targetExitPosFarBlue")),
        "intakeAlignPlayer": mirror_pose(poses.get("IntakeAlignPlayerBlue", require_pose(poses, "intakeAlign1Blue"))),
        "intakePlayer": mirror_pose(poses.get("IntakePlayerBlue", require_pose(poses, "intake1Blue"))),
        "startPose": mirror_pose(start_close if close else start_far),
        "scorePoseGeneral": mirror_pose(score_close if close else score_far),
    }
    mapping["scorePose1"] = mirror_pose(score_close) if force_close_score1 else mapping["scorePoseGeneral"]
    return mapping


def parse_paths(code: str) -> List[Tuple[str, str, str]]:
    paths: List[Tuple[str, str, str]] = []

    direct_pattern = re.compile(
        r"(\w+)\s*=\s*new\s+Path\s*\(\s*new\s+BezierLine\(\s*(\w+)\s*,\s*(\w+)\s*\)\s*\)\s*;"
    )
    helper_pattern = re.compile(r"(\w+)\s*=\s*buildPath\(\s*(\w+)\s*,\s*(\w+)\s*\)\s*;")

    paths.extend(direct_pattern.findall(code))
    paths.extend(helper_pattern.findall(code))
    return paths


def parse_follow_and_wait_sequence(code: str) -> Tuple[List[Tuple[str, bool]], List[float]]:
    body = find_block_after(code, r"private\s+Command\s+autonomousRoutine\s*\(\s*\)")
    if not body:
        return [], []

    var_values: Dict[str, float] = {}
    for var, value in re.findall(r"(?:double|float)\s+(\w+)\s*=\s*([-\d.]+)\s*;", body):
        var_values[var] = float(value)

    token_pattern = re.compile(r"FollowPath\(([^)]*)\)|Delay\(([^)]+)\)")
    follow_order: List[Tuple[str, bool]] = []
    waits: List[float] = []

    for match in token_pattern.finditer(body):
        follow_args = match.group(1)
        delay_expr = match.group(2)

        if follow_args is not None:
            args = [x.strip() for x in follow_args.split(",") if x.strip()]
            if not args:
                continue
            path_name = args[0]
            reverse = any(arg.lower() == "true" for arg in args[1:])
            follow_order.append((path_name, reverse))
        elif delay_expr is not None:
            expr = delay_expr.strip()
            if re.fullmatch(r"[-\d.]+", expr):
                waits.append(float(expr))
            elif expr in var_values:
                waits.append(var_values[expr])

    return follow_order, waits


def build_output(
    runtime_poses: Dict[str, Dict[str, float]],
    path_defs: List[Tuple[str, str, str]],
    follow_order: List[Tuple[str, bool]],
    delays_seconds: List[float],
) -> Dict:
    palette = ["#7B9B79", "#7C8AC7", "#97ACAD", "#B4A7D6", "#F4A261", "#2A9D8F"]
    path_lookup = {name: (start, end) for name, start, end in path_defs}

    lines = []
    sequence = []
    valid_count = 0

    for idx, (path_name, reverse) in enumerate(follow_order):
        if path_name not in path_lookup:
            continue
        start_name, end_name = path_lookup[path_name]
        if start_name not in runtime_poses or end_name not in runtime_poses:
            continue

        start_pose = runtime_poses[start_name]
        end_pose = runtime_poses[end_name]
        line_id = f"{path_name}-{idx}"
        line = {
            "id": line_id,
            "name": path_name.removesuffix("Path"),
            "endPoint": {
                "x": end_pose["x"],
                "y": end_pose["y"],
                "heading": "linear",
                "startDeg": start_pose["deg"],
                "endDeg": end_pose["deg"],
            },
            "controlPoints": [],
            "color": palette[idx % len(palette)],
            "locked": False,
            "waitBeforeMs": 0,
            "waitAfterMs": 0,
            "waitBeforeName": "",
            "waitAfterName": "",
        }
        if reverse:
            line["endPoint"]["reverse"] = True

        lines.append(line)
        sequence.append({"kind": "path", "lineId": line_id})

        if valid_count < len(delays_seconds):
            sequence.append(
                {
                    "kind": "wait",
                    "id": f"wait-{valid_count + 1}",
                    "name": "Wait",
                    "durationMs": int(round(delays_seconds[valid_count] * 1000)),
                    "locked": False,
                }
            )
        valid_count += 1

    return {
        "startPoint": {
            "x": runtime_poses["startPose"]["x"],
            "y": runtime_poses["startPose"]["y"],
            "heading": "linear",
            "startDeg": runtime_poses["startPose"]["deg"],
            "endDeg": runtime_poses["scorePoseGeneral"]["deg"],
            "locked": False,
        },
        "lines": lines,
        "shapes": [],
        "sequence": sequence,
        "settings": {
            "rWidth": 18,
            "rHeight": 18,
            "maxVelocity": 40,
            "maxAcceleration": 30,
            "maxDeceleration": 30,
            "fieldMap": "decode.webp",
            "robotImage": "/robot.png",
            "theme": "auto",
        },
        "version": "1.2.1",
    }


def main() -> None:
    parser = argparse.ArgumentParser(description="Generate a pathing JSON file from a raw FTC auto Java file.")
    parser.add_argument("--input", required=True, help="Path to Java source (or '-' for stdin).")
    parser.add_argument("--output", required=True, help="Path to output JSON.")
    parser.add_argument("--alliance", choices=["blue", "red"], default="blue")
    parser.add_argument("--location", choices=["far", "close"], default="far")
    parser.add_argument("--force-close-score1", action="store_true", help="Use close score pose for score1.")
    parser.add_argument("--start-close-skip-far", action="store_true", help="When location is close, use targetExitPosCloseBlue.")
    args = parser.parse_args()

    raw = Path(args.input).read_text() if args.input != "-" else __import__("sys").stdin.read()
    code = strip_comments(raw)

    poses = parse_pose_constants(code)
    runtime_poses = resolve_runtime_pose_map(
        poses,
        args.alliance,
        args.location,
        args.force_close_score1,
        args.start_close_skip_far,
    )
    path_defs = parse_paths(code)
    follow_order, waits = parse_follow_and_wait_sequence(code)
    output = build_output(runtime_poses, path_defs, follow_order, waits)

    Path(args.output).write_text(json.dumps(output, indent=2) + "\n")


if __name__ == "__main__":
    main()
