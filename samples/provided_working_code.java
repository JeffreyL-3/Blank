package org.firstinspires.ftc.teamcode.autos;

import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.Path;
import dev.nextftc.core.commands.Command;
import dev.nextftc.core.commands.delays.Delay;
import dev.nextftc.core.commands.groups.ParallelGroup;
import dev.nextftc.extensions.pedro.FollowPath;

public class ANewWorkingAuto {
    public static final Pose startPoseFarBlue = new Pose(56, 8, Math.toRadians(270));
    public static final Pose startPoseCloseBlue = new Pose(20, 123, Math.toRadians(323));
    public static final Pose scorePoseCloseBlue = new Pose(56, 81, Math.toRadians(315));
    public static final Pose scorePoseFarBlue = new Pose(59, 18, Math.toRadians(294));

    public static final Pose intakeAlign1Blue = new Pose(45, 84, Math.toRadians(180));
    public static final Pose intake1Blue = new Pose(12, 84, Math.toRadians(180));

    public static final Pose intakeAlign2Blue = new Pose(45, 58, Math.toRadians(180));
    public static final Pose intake2Blue = new Pose(6, 58, Math.toRadians(180));

    public static final Pose intakeAlign3Blue = new Pose(45, 36, Math.toRadians(180));
    public static final Pose intake3Blue = new Pose(6, 36, Math.toRadians(180));

    public static final Pose IntakePlayerBlue = new Pose(12, 10, Math.toRadians(210));
    public static final Pose IntakeAlignPlayerBlue = new Pose(11,17, Math.toRadians(185));

    public static final Pose targetExitPosFarBlue = new Pose(50, 35, Math.toRadians(295));
    public static final Pose targetExitPosCloseBlue = new Pose(56, 81, Math.toRadians(315));

    Pose startPose;
    Pose scorePoseGeneral;
    Pose scorePose1;

    Pose intakeAlign1;
    Pose intake1;
    Pose intakeAlign2;
    Pose intake2;
    Pose intakeAlign3;
    Pose intake3;
    Pose targetExitPos;
    Pose intakeAlignPlayer;
    Pose intakePlayer;

    Path scorePreloadPath;
    Path intakeAlign1Path;
    Path intake1Path;
    Path score1Path;
    Path intakeAlign2Path;
    Path intake2Path;
    Path score2Path;
    Path intakeAlign3Path;
    Path intake3Path;
    Path score3Path;
    Path finalExitPath;
    Path intakeAlignPlayerPath;
    Path intakePlayerPath;
    Path scorePlayerPath;

    private Command autonomousRoutine() {
        double standardDelay = 0.025;
        return new SequentialGroupFixed(
                new FollowPath(scorePreloadPath),
                new Delay(standardDelay),
                new FollowPath(intakeAlign2Path),
                new Delay(standardDelay),
                new ParallelGroup(
                        new FollowPath(intake2Path, true, 0.5),
                        new Delay (0.05)
                ),
                new Delay(standardDelay),
                new FollowPath(score2Path),
                new Delay(standardDelay),
                new FollowPath(intakeAlign1Path),
                new Delay(standardDelay),
                new ParallelGroup(
                        new FollowPath(intake1Path, true, 0.5),
                        new Delay (0.05)
                ),
                new Delay(standardDelay),
                new FollowPath(score1Path),
                new Delay(standardDelay),
                new FollowPath(intakeAlign3Path),
                new Delay(standardDelay),
                new ParallelGroup(
                        new FollowPath(intake3Path, true, 0.5),
                        new Delay (0.05)
                ),
                new Delay(standardDelay),
                new FollowPath(score3Path),
                new Delay(standardDelay),
                new FollowPath(finalExitPath)
        );
    }

    public void onStartButtonPressed() {
        intakeAlign1=intakeAlign1Blue;
        intake1 = intake1Blue;
        intakeAlign2 = intakeAlign2Blue;
        intake2 = intake2Blue;
        intakeAlign3 = intakeAlign3Blue;
        intake3 = intake3Blue;
        targetExitPos = targetExitPosFarBlue;
        intakeAlignPlayer = IntakeAlignPlayerBlue;
        intakePlayer = IntakePlayerBlue;
        startPose = startPoseFarBlue;
        scorePoseGeneral = scorePoseFarBlue;
        scorePose1 = scorePoseGeneral;

        scorePreloadPath = new Path(new BezierLine(startPose, scorePoseGeneral));
        intakeAlign2Path = new Path(new BezierLine(scorePoseGeneral, intakeAlign2));
        intake2Path = new Path(new BezierLine(intakeAlign2, intake2));
        score2Path = new Path(new BezierLine(intake2, scorePoseGeneral));
        intakeAlign1Path = new Path(new BezierLine(scorePoseGeneral, intakeAlign1));
        intake1Path = new Path(new BezierLine(intakeAlign1, intake1));
        score1Path = new Path(new BezierLine(intake1, scorePose1));
        intakeAlign3Path = new Path(new BezierLine(scorePose1, intakeAlign3));
        intake3Path = new Path(new BezierLine(intakeAlign3, intake3));
        score3Path = new Path(new BezierLine(intake3, scorePoseGeneral));
        finalExitPath = new Path(new BezierLine(scorePoseGeneral, targetExitPos));
        intakeAlignPlayerPath = new Path(new BezierLine(scorePoseGeneral, intakeAlignPlayer));
        intakePlayerPath = new Path(new BezierLine(intakeAlignPlayer, intakePlayer));
        scorePlayerPath = new Path(new BezierLine(intakePlayer, scorePoseGeneral));
    }
}
