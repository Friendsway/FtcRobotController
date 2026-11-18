package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.IMU;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcode.config.RobotConstants;
import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;

/*
 * Headless Mecanum Drive OpMode in opmodes.teleop package.
 */
@TeleOp(name = "Mecanum Drive Headless (Organized)", group = "StarterBot")
@Disabled
public class MecanumDriveOnlyHeadless extends OpMode {

    private Drivetrain drivetrain;
    private IMU imu;

    @Override
    public void init() {
        drivetrain = new Drivetrain(hardwareMap);

        imu = hardwareMap.get(IMU.class, RobotConstants.IMU_SENSOR);
        RevHubOrientationOnRobot orientationOnRobot = new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.UP,
                RevHubOrientationOnRobot.UsbFacingDirection.FORWARD);
        imu.initialize(new IMU.Parameters(orientationOnRobot));

        telemetry.addData("Status", "Initialized - Headless IMU Drive Ready");
    }

    @Override
    public void loop() {
        if (gamepad1.a) {
            imu.resetYaw();
        }

        double forward = -gamepad1.left_stick_y;
        double strafe = -gamepad1.left_stick_x;
        double rotate = -gamepad1.right_stick_x;

        if (gamepad1.left_bumper) {
            drivetrain.mecanumDrive(forward, strafe, rotate);
        } else {
            double botHeading = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);
            double rotStrafe = strafe * Math.cos(-botHeading) - forward * Math.sin(-botHeading);
            double rotForward = strafe * Math.sin(-botHeading) + forward * Math.cos(-botHeading);

            drivetrain.mecanumDrive(rotForward, rotStrafe, rotate);
        }

        double yawDeg = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
        telemetry.addData("Drive Mode", gamepad1.left_bumper ? "Robot-Centric" : "Headless (Field-Centric)");
        telemetry.addData("Robot Heading", "%.2f Deg", yawDeg);
    }
}
