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

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;

/*
 * TeleOp OpMode for Mecanum Drivetrain in Headless (Field-Centric) Mode using
 * the goBILDA® Pinpoint Odometry Computer with 4-Bar Odometry Pods.
 *
 * In Headless Mode:
 * - Pushing UP on the left stick moves the robot field-forward (away from the driver)
 *   regardless of which direction the robot is facing.
 * - Press 'A' on Gamepad 1 to reset the field-forward heading direction.
 * - Hold 'Left Bumper' on Gamepad 1 to temporarily switch to standard Robot-Centric driving.
 */

@TeleOp(name = "TeleopMecanumHeadless StarterBot Teleop", group = "StarterBot")
//@Disabled
public class TeleopMecanumHeadless extends OpMode {

    // Declare OpMode members.
    private DcMotor leftFrontDrive = null;
    private DcMotor leftBackDrive = null;
    private DcMotor rightFrontDrive = null;
    private DcMotor rightBackDrive = null;

    // GoBilda Pinpoint Odometry Computer
    private GoBildaPinpointDriver pinpoint = null;

    /*
     * Variables to store calculated wheel powers.
     */
    double leftFrontPower;
    double rightFrontPower;
    double leftBackPower;
    double rightBackPower;

    /*
     * Code to run ONCE when the driver hits INIT
     */
    @Override
    public void init() {

        /*
         * Initialize the hardware variables.
         */
        leftFrontDrive = hardwareMap.get(DcMotor.class, "left_front_drive");
        rightFrontDrive = hardwareMap.get(DcMotor.class, "right_front_drive");
        leftBackDrive = hardwareMap.get(DcMotor.class, "left_back_drive");
        rightBackDrive = hardwareMap.get(DcMotor.class, "right_back_drive");

        /*
         * Initialize the goBILDA Pinpoint Odometry Computer.
         * Configured in Robot Controller with device name "pinpoint".
         */
        pinpoint = hardwareMap.get(GoBildaPinpointDriver.class, "pinpoint");

        /*
         * Configure Pinpoint for the goBILDA 4-Bar Odometry Pods.
         */
        configurePinpoint();

        /*
         * Set motor directions for Mecanum drivetrain.
         */
        leftFrontDrive.setDirection(DcMotor.Direction.REVERSE);
        rightFrontDrive.setDirection(DcMotor.Direction.FORWARD);
        leftBackDrive.setDirection(DcMotor.Direction.REVERSE);
        rightBackDrive.setDirection(DcMotor.Direction.FORWARD);

        /*
         * Enable brake mode for controllable stopping.
         */
        leftFrontDrive.setZeroPowerBehavior(BRAKE);
        rightFrontDrive.setZeroPowerBehavior(BRAKE);
        leftBackDrive.setZeroPowerBehavior(BRAKE);
        rightBackDrive.setZeroPowerBehavior(BRAKE);

        /*
         * Tell the driver that initialization is complete.
         */
        telemetry.addData("Status", "Initialized - Headless Mode Ready");
    }

    /*
     * Code to run REPEATEDLY after the driver hits INIT, but before they hit START
     */
    @Override
    public void init_loop() {
    }

    /*
     * Code to run ONCE when the driver hits START
     */
    @Override
    public void start() {
    }

    /*
     * Code to run REPEATEDLY after the driver hits START but before they hit STOP
     */
    @Override
    public void loop() {
        // Update Pinpoint sensor data (must be called every loop cycle)
        pinpoint.update();

        // Reset field heading if 'A' button is pressed
        if (gamepad1.a) {
            pinpoint.resetPosAndIMU();
        }

        double forward = gamepad1.left_stick_y;
        double strafe = gamepad1.left_stick_x;
        double rotate = gamepad1.right_stick_x;

        if (gamepad1.left_bumper) {
            // Robot-Centric Drive when holding Left Bumper
            mecanumDrive(forward, strafe, rotate);
        } else {
            // Headless / Field-Centric Drive
            double botHeading = pinpoint.getPosition().getHeading(AngleUnit.RADIANS);

            // Rotate the translation vector by negative robot heading
            double rotStrafe = strafe * Math.cos(-botHeading) - forward * Math.sin(-botHeading);
            double rotForward = strafe * Math.sin(-botHeading) + forward * Math.cos(-botHeading);

            mecanumDrive(rotForward, rotStrafe, rotate);
        }

        // Send telemetry to Driver Station
        Pose2D pose = pinpoint.getPosition();
        telemetry.addData("Drive Mode", gamepad1.left_bumper ? "Robot-Centric" : "Headless (Field-Centric)");
        telemetry.addData("Heading (Deg)", "%.2f", pose.getHeading(AngleUnit.DEGREES));
        telemetry.addData("X (Inches)", "%.2f", pose.getX(DistanceUnit.INCH));
        telemetry.addData("Y (Inches)", "%.2f", pose.getY(DistanceUnit.INCH));
        telemetry.addData("Motors", "LF (%.2f), RF (%.2f), LB (%.2f), RB (%.2f)",
                leftFrontPower, rightFrontPower, leftBackPower, rightBackPower);
    }

    /*
     * Code to run ONCE after the driver hits STOP
     */
    @Override
    public void stop() {
    }

    /**
     * Configures the goBILDA Pinpoint computer settings for 4-Bar Odometry Pods.
     */
    private void configurePinpoint() {
        /*
         * Pod Offsets relative to the center of tracking (in millimeters).
         * X offset: distance left(+) or right(-) from center for forward pod.
         * Y offset: distance forward(+) or backward(-) from center for strafe pod.
         * Adjust these values to match your physical pod mounting location.
         */
        pinpoint.setOffsets(-84.0, -168.0, DistanceUnit.MM);

        /*
         * Set pod encoder type to goBILDA 4-Bar Odometry Pods.
         */
        pinpoint.setEncoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);

        /*
         * Set encoder directions. Change to REVERSE if a pod counts down when moving in positive direction.
         */
        pinpoint.setEncoderDirections(GoBildaPinpointDriver.EncoderDirection.FORWARD,
                                      GoBildaPinpointDriver.EncoderDirection.FORWARD);

        /*
         * Reset position coordinates and recalibrate IMU heading.
         */
        pinpoint.resetPosAndIMU();
    }

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

        /*
         * Send calculated power to wheels
         */
        leftFrontDrive.setPower(leftFrontPower);
        rightFrontDrive.setPower(rightFrontPower);
        leftBackDrive.setPower(leftBackPower);
        rightBackDrive.setPower(rightBackPower);
    }
}
