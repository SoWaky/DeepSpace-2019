/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.Subsystems;

import com.revrobotics.CANPIDController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.ControlType;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.command.Subsystem;
import frc.robot.Constants;
import frc.robot.Commands.ManualCommandDrive;
import frc.robot.Util.Xbox;

/**
 * Subsystem controlling the motors in the drivetrain
 */
public class SubsystemDrive extends Subsystem {

  private static CANSparkMax leftMaster;
  private static CANSparkMax leftSlave;
  private static CANSparkMax rightMaster;
  private static CANSparkMax rightSlave;

  private static CANPIDController leftPID;
  private static CANPIDController rightPID;

  @Override
  public void initDefaultCommand() {
    setDefaultCommand(new ManualCommandDrive());
  }

  public SubsystemDrive() {
    DriverStation.reportWarning("SUB_DRIVE CREATED", false);
    leftMaster  = new CANSparkMax(Constants.LEFT_MASTER_ID, MotorType.kBrushless);
    leftSlave   = new CANSparkMax(Constants.LEFT_SLAVE_ID, MotorType.kBrushless);

    rightMaster = new CANSparkMax(Constants.RIGHT_MASTER_ID, MotorType.kBrushless);
    rightSlave  = new CANSparkMax(Constants.RIGHT_SLAVE_ID, MotorType.kBrushless);
  }

  /**
   * Rocket League/Tank hybrid control system
   * Left and right triggers accelerate linearly and left stick rotates
   * @param joy the joystick to be used
   */
  public void driveRLTank(Joystick joy, double ramp) {
    setInverts();
    setBraking(true);
    setRamps(ramp);

    double adder = Xbox.RT(joy) - Xbox.LT(joy);
    double left = adder + (Xbox.LEFT_X(joy) / 1.333333);
    double right = adder - (Xbox.LEFT_X(joy) / 1.333333);
    left = (left > 1.0 ? 1.0 : (left < -1.0 ? -1.0 : left));
    right = (right > 1.0 ? 1.0 : (right < -1.0 ? -1.0 : right));
    
    leftMaster.set(left);
      leftSlave.set(left);
    rightMaster.set(right);
      rightSlave.set(right);
  }

  public void driveByPosition(double inches, double[] PID) {
    inches *= Constants.ENCODER_TICKS_PER_ROTATION;

    leftPID = new CANPIDController(leftMaster);
    rightPID = new CANPIDController(rightMaster);

    leftPID.setP(PID[0]);
      leftPID.setI(PID[1]);
      leftPID.setD(PID[2]);
    rightPID.setP(PID[0]);
      rightPID.setI(PID[1]);
      rightPID.setD(PID[2]);

    leftPID.setReference(inches, ControlType.kPosition);
    rightPID.setReference(inches, ControlType.kPosition);
  }

  public double[] getError(double[] initEncoderPositions) {
    double[] output = new double[2];
    output[0] = initEncoderPositions[0] - leftMaster.getEncoder().getPosition();
    output[1] = initEncoderPositions[1] - rightMaster.getEncoder().getPosition();
    return output;
  }

  public double[] getEncoderPositions() {
    double[] output = new double[2];
    output[0] = leftMaster.getEncoder().getPosition();
    output[1] = rightMaster.getEncoder().getPosition();
    return output;
  }

  public double[] getAppliedOutputs() {
    return new double[]{ leftMaster.getAppliedOutput(), rightMaster.getAppliedOutput() };
  }

  public void stopMotors() {
    leftMaster.stopMotor();
      leftSlave.stopMotor();
    rightMaster.stopMotor();
      rightSlave.stopMotor();
  }

  /**
   * Sets the inverts of each motor controller
   */
  private void setInverts() {
    leftMaster.setInverted(Constants.LEFT_DRIVE_INVERT);
      leftSlave.setInverted(Constants.LEFT_DRIVE_INVERT);
    rightMaster.setInverted(Constants.RIGHT_DRIVE_INVERT);
      rightSlave.setInverted(Constants.RIGHT_DRIVE_INVERT);
  }

  /**
   * Sets each motor to braking or coasting mode
   * @param braking true if braking mode, false if coasting mode
   */
  private void setBraking(Boolean braking) {
    leftMaster.setIdleMode(braking ? IdleMode.kBrake : IdleMode.kCoast);
      leftSlave.setIdleMode(braking ? IdleMode.kBrake : IdleMode.kCoast);
    rightMaster.setIdleMode(braking ? IdleMode.kBrake : IdleMode.kCoast);
      rightSlave.setIdleMode(braking ? IdleMode.kBrake : IdleMode.kCoast);
  }

  /**
   * Sets the ramp rate of each motor
   * @param ramp ramp rate in seconds
   */
  private void setRamps(double ramp) {
    leftMaster.setRampRate(ramp);
      leftSlave.setRampRate(ramp);
    rightMaster.setRampRate(ramp);
      rightSlave.setRampRate(ramp);
  }

  /**
   * Retrieves the percentOutput/speed values of each motor controller
   * @return array of percentOutputs/speeds stored as doubles
   *         [0] = Left Master speed
   *         [1] = Left Slave speed
   *         [2] = Right Master speed
   *         [3] = Right Slave speed
   */
  public double[] getMotorValues() {
    double[] output = new double[4];
    output[0] = leftMaster.get();
    output[1] = leftSlave.get();
    output[2] = rightMaster.get();
    output[3] = rightSlave.get();
    return output;
  }

  
}
