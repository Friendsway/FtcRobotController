/*   MIT License
 *   Copyright (c) [2026] [Base 10 Assets, LLC]
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:

 *   The above copyright notice and this permission notice shall be included in all
 *   copies or substantial portions of the Software.

 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *   SOFTWARE.
 */

package org.firstinspires.ftc.teamcode;

import static com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior.BRAKE;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.VoltageSensor;

/*
 * Upgraded Mecanum Drive OpMode including:
 * 1. Exponential/Cubic Joystick Curves (Feature 2) - Smooth precision at low speeds.
 * 2. Joystick Deadzones (Feature 3) - Eliminates stick drift creep.
 * 3. Battery Voltage Compensation (Feature 4) - Consistent power as battery drains.
 *
 * Hardware Map Names Required:
 * - "left_front_drive"
 * - "right_front_drive"
 * - "left_back_drive"
 * - "right_back_drive"
 */

@TeleOp(name = "Mecanum Drive Upgrades", group = "StarterBot")
//@Disabled
public class MecanumDriveOnlyUpgrades extends OpMode {

    // Threshold below which joystick drift is ignored
    public static final double JOYSTICK_DEADZONE = 0.05;

    // Target voltage for voltage compensation
    public static final double NOMINAL_VOLTAGE = 12.0;

    // Mecanum wheel motors
    private DcMotor leftFrontDrive = null;
    private DcMotor leftBackDrive = null;
    private DcMotor rightFrontDrive = null;
    private DcMotor rightBackDrive = null;

    // Battery Voltage Sensor
    private VoltageSensor batteryVoltageSensor = null;

    // Motor powers for telemetry
    double leftFrontPower;
    double rightFrontPower;
    double leftBackPower;
    double rightBackPower;

    @Override
    public void init() {
        leftFrontDrive = hardwareMap.get(DcMotor.class, "left_front_drive");
        rightFrontDrive = hardwareMap.get(DcMotor.class, "right_front_drive");
        leftBackDrive = hardwareMap.get(DcMotor.class, "left_back_drive");
        rightBackDrive = hardwareMap.get(DcMotor.class, "right_back_drive");

        leftFrontDrive.setDirection(DcMotor.Direction.FORWARD);
        rightFrontDrive.setDirection(DcMotor.Direction.REVERSE);
        leftBackDrive.setDirection(DcMotor.Direction.FORWARD);
        rightBackDrive.setDirection(DcMotor.Direction.REVERSE);

        leftFrontDrive.setZeroPowerBehavior(BRAKE);
        rightFrontDrive.setZeroPowerBehavior(BRAKE);
        leftBackDrive.setZeroPowerBehavior(BRAKE);
        rightBackDrive.setZeroPowerBehavior(BRAKE);

        // Get voltage sensor from Control Hub
        if (hardwareMap.voltageSensor.iterator().hasNext()) {
            batteryVoltageSensor = hardwareMap.voltageSensor.iterator().next();
        }

        telemetry.addData("Status", "Initialized - Upgraded Mecanum Drive Ready");
    }

    @Override
    public void init_loop() {
    }

    @Override
    public void start() {
    }

    @Override
    public void loop() {
        // Read gamepad inputs and process through Deadzone + Cubic Curve
        double rawForward = -gamepad1.left_stick_y;
        double rawStrafe = -gamepad1.left_stick_x;
        double rawRotate = -gamepad1.right_stick_x;

        double forward = applyCubicCurveWithDeadzone(rawForward);
        double strafe = applyCubicCurveWithDeadzone(rawStrafe);
        double rotate = applyCubicCurveWithDeadzone(rawRotate);

        mecanumDrive(forward, strafe, rotate);

        // Telemetry
        double currentVoltage = (batteryVoltageSensor != null) ? batteryVoltageSensor.getVoltage() : NOMINAL_VOLTAGE;
        telemetry.addData("Battery Voltage", "%.2f V", currentVoltage);
        telemetry.addData("Front Motors", "LF (%.2f), RF (%.2f)", leftFrontPower, rightFrontPower);
        telemetry.addData("Back Motors",  "LB (%.2f), RB (%.2f)", leftBackPower, rightBackPower);
    }

    @Override
    public void stop() {
    }

    /**
     * Applies deadzone filter to remove drift, then applies cubic curve scaling.
     */
    private double applyCubicCurveWithDeadzone(double input) {
        if (Math.abs(input) < JOYSTICK_DEADZONE) {
            return 0.0;
        }
        // Cubic curve: input^3
        return input * input * input;
    }

    /**
     * Calculates and assigns normalized power to Mecanum wheel motors with voltage compensation.
     */
    void mecanumDrive(double forward, double strafe, double rotate) {
        leftFrontPower = forward + strafe + rotate;
        rightFrontPower = forward - strafe - rotate;
        leftBackPower = forward - strafe + rotate;
        rightBackPower = forward + strafe - rotate;

        double max = Math.max(Math.abs(leftFrontPower), Math.abs(rightFrontPower));
        max = Math.max(max, Math.abs(leftBackPower));
        max = Math.max(max, Math.abs(rightBackPower));

        if (max > 1.0) {
            leftFrontPower /= max;
            rightFrontPower /= max;
            leftBackPower /= max;
            rightBackPower /= max;
        }

        // Apply Voltage Compensation scaling
        if (batteryVoltageSensor != null) {
            double voltage = batteryVoltageSensor.getVoltage();
            if (voltage > 8.0) {
                double compFactor = NOMINAL_VOLTAGE / voltage;
                leftFrontPower *= compFactor;
                rightFrontPower *= compFactor;
                leftBackPower *= compFactor;
                rightBackPower *= compFactor;
            }
        }

        leftFrontDrive.setPower(leftFrontPower);
        rightFrontDrive.setPower(rightFrontPower);
        leftBackDrive.setPower(leftBackPower);
        rightBackDrive.setPower(rightBackPower);
    }
}
