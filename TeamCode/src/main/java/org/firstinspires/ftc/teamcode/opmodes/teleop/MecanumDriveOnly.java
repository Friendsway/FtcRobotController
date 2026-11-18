package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;

/*
 * Basic Mecanum Drive OpMode in opmodes.teleop package using Drivetrain subsystem.
 */
@TeleOp(name = "Mecanum Drive Only (Organized)", group = "StarterBot")
@Disabled
public class MecanumDriveOnly extends OpMode {

    private Drivetrain drivetrain;

    @Override
    public void init() {
        drivetrain = new Drivetrain(hardwareMap);
        telemetry.addData("Status", "Initialized - Mecanum Drive Only Ready");
    }

    @Override
    public void loop() {
        double forward = -gamepad1.left_stick_y;
        double strafe = -gamepad1.left_stick_x;
        double rotate = -gamepad1.right_stick_x;

        drivetrain.mecanumDrive(forward, strafe, rotate);
        telemetry.addData("Status", "Running");
    }
}
