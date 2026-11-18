package org.firstinspires.ftc.teamcode.config;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * Centralized Hardware Configuration and Tuning Constants for TeamCode.
 */
public class RobotConstants {

    // Drivetrain Motor Hardware Map Names
    public static final String LEFT_FRONT_MOTOR = "left_front_drive";
    public static final String RIGHT_FRONT_MOTOR = "right_front_drive";
    public static final String LEFT_BACK_MOTOR = "left_back_drive";
    public static final String RIGHT_BACK_MOTOR = "right_back_drive";

    // Non-Mecanum Drivetrain Motors
    public static final String LEFT_DRIVE_MOTOR = "left_drive";
    public static final String RIGHT_DRIVE_MOTOR = "right_drive";

    // Subsystem Motor & Servo Hardware Map Names
    public static final String INTAKE_MOTOR = "intake";
    public static final String LAUNCHER_MOTOR = "launcher";
    public static final String WINDMILL_SERVO = "windmill";
    public static final String LEFT_INTAKE_SERVO = "left_intake_servo";
    public static final String RIGHT_INTAKE_SERVO = "right_intake_servo";

    // Sensors
    public static final String PINPOINT_SENSOR = "pinpoint";
    public static final String IMU_SENSOR = "imu";

    // Tuning Parameters
    public static final double JOYSTICK_DEADZONE = 0.05;
    public static final double NOMINAL_VOLTAGE = 12.0;

    // Pinpoint Pod Offsets
    public static final double X_POD_OFFSET_MM = -84.0;
    public static final double Y_POD_OFFSET_MM = -168.0;
    public static final DistanceUnit OFFSET_UNIT = DistanceUnit.MM;
}
