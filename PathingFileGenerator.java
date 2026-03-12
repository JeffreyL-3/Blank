import java.time.Instant;

/**
 * Basic generator for a Pedro-style pathing JSON file using poses from ANewWorkingAuto.
 * This version is locked to BLUE + FAR start.
 */
public class PathingFileGenerator {

    private static final Pose START_POSE_FAR_BLUE = new Pose(56, 8, 270);
    private static final Pose SCORE_POSE_FAR_BLUE = new Pose(59, 18, 293.83);

    private static final Pose INTAKE_ALIGN_2_BLUE = new Pose(45, 58, 180);
    private static final Pose INTAKE_2_BLUE = new Pose(6, 58, 180);

    private static final Pose INTAKE_ALIGN_1_BLUE = new Pose(45, 84, 180);
    private static final Pose INTAKE_1_BLUE = new Pose(12, 84, 180);

    private static final Pose INTAKE_ALIGN_3_BLUE = new Pose(45, 36, 180);
    private static final Pose INTAKE_3_BLUE = new Pose(6, 36, 180);

    private static final Pose TARGET_EXIT_POS_FAR_BLUE = new Pose(50, 35, 295);

    public static void main(String[] args) {
        String timestamp = Instant.now().toString();

        String json = """
                {
                  "startPoint": {
                    "x": %s,
                    "y": %s,
                    "heading": "linear",
                    "startDeg": %s,
                    "endDeg": %s,
                    "locked": false
                  },
                  "lines": [
                    %s,
                    %s,
                    %s,
                    %s,
                    %s,
                    %s,
                    %s,
                    %s,
                    %s,
                    %s,
                    %s
                  ],
                  "shapes": [
                    {
                      "id": "triangle-1",
                      "name": "Red Goal",
                      "vertices": [
                        { "x": 144, "y": 70 },
                        { "x": 144, "y": 144 },
                        { "x": 120, "y": 144 },
                        { "x": 138, "y": 119 },
                        { "x": 138, "y": 70 }
                      ],
                      "color": "#dc2626",
                      "fillColor": "#ff6b6b"
                    },
                    {
                      "id": "triangle-2",
                      "name": "Blue Goal",
                      "vertices": [
                        { "x": 6, "y": 119 },
                        { "x": 25, "y": 144 },
                        { "x": 0, "y": 144 },
                        { "x": 0, "y": 70 },
                        { "x": 7, "y": 70 }
                      ],
                      "color": "#2563eb",
                      "fillColor": "#60a5fa"
                    }
                  ],
                  "sequence": [
                    { "kind": "path", "lineId": "scorePreloadBlueFar" },
                    { "kind": "path", "lineId": "intakeAlign2BlueFar" },
                    { "kind": "path", "lineId": "intake2BlueFar" },
                    { "kind": "path", "lineId": "intakeAlign2OutBlueFar" },
                    { "kind": "path", "lineId": "score2BlueFar" },
                    { "kind": "path", "lineId": "intakeAlign1BlueFar" },
                    { "kind": "path", "lineId": "intake1BlueFar" },
                    { "kind": "path", "lineId": "score1BlueFar" },
                    { "kind": "path", "lineId": "intakeAlign3BlueFar" },
                    { "kind": "path", "lineId": "intake3BlueFar" },
                    { "kind": "path", "lineId": "score3BlueFar" },
                    { "kind": "path", "lineId": "finalExitBlueFar" }
                  ],
                  "settings": {
                    "xVelocity": 75,
                    "yVelocity": 65,
                    "aVelocity": 3.141592653589793,
                    "kFriction": 0.1,
                    "rWidth": 16,
                    "rHeight": 16,
                    "safetyMargin": 1,
                    "maxVelocity": 40,
                    "maxAcceleration": 30,
                    "maxDeceleration": 30,
                    "fieldMap": "decode.webp",
                    "robotImage": "/robot.png",
                    "theme": "auto",
                    "showGhostPaths": false,
                    "showOnionLayers": false,
                    "onionLayerSpacing": 3,
                    "onionColor": "#dc2626",
                    "onionNextPointOnly": false
                  },
                  "version": "1.2.1",
                  "timestamp": "%s"
                }
                """.formatted(
                asNumber(START_POSE_FAR_BLUE.x),
                asNumber(START_POSE_FAR_BLUE.y),
                asNumber(START_POSE_FAR_BLUE.headingDeg),
                asNumber(SCORE_POSE_FAR_BLUE.headingDeg),
                lineJson("scorePreloadBlueFar", "scorePoseFarBlue", SCORE_POSE_FAR_BLUE, false, START_POSE_FAR_BLUE.headingDeg, false),
                lineJson("intakeAlign2BlueFar", "intakeAlign2Blue", INTAKE_ALIGN_2_BLUE, true, SCORE_POSE_FAR_BLUE.headingDeg, true),
                lineJson("intake2BlueFar", "intake2Blue", INTAKE_2_BLUE, true, INTAKE_ALIGN_2_BLUE.headingDeg, true),
                lineJson("intakeAlign2OutBlueFar", "intakeAlign2OutBlue", INTAKE_ALIGN_2_BLUE, true, INTAKE_2_BLUE.headingDeg, true),
                lineJson("score2BlueFar", "scorePoseFarBlue", SCORE_POSE_FAR_BLUE, true, INTAKE_ALIGN_2_BLUE.headingDeg, true),
                lineJson("intakeAlign1BlueFar", "intakeAlign1Blue", INTAKE_ALIGN_1_BLUE, true, SCORE_POSE_FAR_BLUE.headingDeg, true),
                lineJson("intake1BlueFar", "intake1Blue", INTAKE_1_BLUE, true, INTAKE_ALIGN_1_BLUE.headingDeg, true),
                lineJson("score1BlueFar", "scorePoseFarBlue", SCORE_POSE_FAR_BLUE, false, INTAKE_1_BLUE.headingDeg, false),
                lineJson("intakeAlign3BlueFar", "intakeAlign3Blue", INTAKE_ALIGN_3_BLUE, true, SCORE_POSE_FAR_BLUE.headingDeg, true),
                lineJson("intake3BlueFar", "intake3Blue", INTAKE_3_BLUE, true, INTAKE_ALIGN_3_BLUE.headingDeg, true),
                lineJson("score3BlueFar", "scorePoseFarBlue", SCORE_POSE_FAR_BLUE, true, INTAKE_3_BLUE.headingDeg, true),
                lineJson("finalExitBlueFar", "targetExitPosFarBlue", TARGET_EXIT_POS_FAR_BLUE, true, SCORE_POSE_FAR_BLUE.headingDeg, true),
                timestamp
        );

        System.out.println(json);
    }

    private static String lineJson(String id, String name, Pose endPose, boolean reverse, double startDeg,
                                   boolean constantHeadingInterpolation) {
        double headingStartDeg = constantHeadingInterpolation ? endPose.headingDeg : startDeg;
        String headingType = constantHeadingInterpolation ? "constant" : "linear";

        return """
                {
                  "id": "%s",
                  "name": "%s",
                  "endPoint": {
                    "x": %s,
                    "y": %s,
                    "heading": "%s",
                    "reverse": %s,
                    "startDeg": %s,
                    "endDeg": %s
                  },
                  "controlPoints": [],
                  "color": "#7B9B79",
                  "locked": false,
                  "waitBeforeMs": 0,
                  "waitAfterMs": 0,
                  "waitBeforeName": "",
                  "waitAfterName": ""
                }
                """.formatted(
                id,
                name,
                asNumber(endPose.x),
                asNumber(endPose.y),
                headingType,
                reverse,
                asNumber(headingStartDeg),
                asNumber(endPose.headingDeg)
        );
    }

    private static String asNumber(double value) {
        if (Math.floor(value) == value) {
            return String.valueOf((int) value);
        }
        return String.valueOf(value);
    }

    private record Pose(double x, double y, double headingDeg) {}
}
