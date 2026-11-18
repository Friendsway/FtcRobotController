package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import org.firstinspires.ftc.teamcode.config.RobotConstants;
import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;

/*
 * Upgraded Mecanum Drive OpMode in opmodes.teleop package.
 */
@TeleOp(name = "Mecanum Drive Upgrades (Organized)", group = "StarterBot")
@Disabled
public class MecanumDriveOnlyUpgrades extends OpMode {

    private Drivetrain drivetrain;
    private VoltageSensor batteryVoltageSensor = null;

    @Override
    public void init() {
        drivetrain = new Drivetrain(hardwareMap);
        if (hardwareMap.voltageSensor.iterator().hasNext()) {
            batteryVoltageSensor = hardwareMap.voltageSensor.iterator().next();
        }
        telemetry.addData("Status", "Initialized - Upgraded Mecanum Drive Ready");
    }

    @Override
    public void loop() {
        double rawForward = -gamepad1.left_stick_y;
        double rawStrafe = -gamepad1.left_stick_x;
        double rawRotate = -gamepad1.right_stick_x;

        double forward = applyCubicCurveWithDeadzone(rawForward);
        double strafe = applyCubicCurveWithDeadzone(rawStrafe);
        double rotate = applyCubicCurveWithDeadzone(rawRotate);

        if (batteryVoltageSensor != null) {
            double voltage = batteryVoltageSensor.getVoltage();
            if (voltage > 8.0) {
                double compFactor = RobotConstants.NOMINAL_VOLTAGE / voltage;
                forward *= compFactor;
                strafe *= compFactor;
                rotate *= compFactor;
            }
        }

        drivetrain.mecanumDrive(forward, strafe, rotate);

        double currentVoltage = (batteryVoltageSensor != null) ? batteryVoltageSensor.getVoltage() : RobotConstants.NOMINAL_VOLTAGE;
        telemetry.addData("Battery Voltage", "%.2f V", currentVoltage);
    }

    private double applyCubicCurveWithDeadzone(double input) {
        if (Math.abs(input) < RobotConstants.JOYSTICK_DEADZONE) {
            return 0.0;
        }
        return input * input * input;
    }
}
