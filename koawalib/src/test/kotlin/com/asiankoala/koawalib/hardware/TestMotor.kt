package com.asiankoala.koawalib.hardware

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorController
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType

internal class TestMotor : TestDevice(), DcMotor {
    private var power = 0.0
    private var position = 0
    private var direction = DcMotorSimple.Direction.FORWARD
    private var mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
    private var zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
    
    override fun setDirection(direction: DcMotorSimple.Direction) {
        this.direction = direction
    }

    override fun getDirection(): DcMotorSimple.Direction = direction

    override fun setPower(power: Double) {
        this.power = power
    }

    override fun getPower(): Double = power

    override fun getMotorType(): MotorConfigurationType = MotorConfigurationType.getUnspecifiedMotorType()

    override fun setMotorType(motorType: MotorConfigurationType) {}

    override fun getController(): DcMotorController? = null

    override fun getPortNumber(): Int = -1

    override fun setZeroPowerBehavior(zeroPowerBehavior: DcMotor.ZeroPowerBehavior) {
        this.zeroPowerBehavior = zeroPowerBehavior
    }

    override fun getZeroPowerBehavior(): DcMotor.ZeroPowerBehavior = zeroPowerBehavior

    override fun setPowerFloat() {}

    override fun getPowerFloat(): Boolean = false

    override fun setTargetPosition(position: Int) {}

    override fun getTargetPosition(): Int = Int.MIN_VALUE

    override fun isBusy(): Boolean = false

    override fun getCurrentPosition(): Int = position

    override fun setMode(mode: DcMotor.RunMode) {
        this.mode = mode
    }

    override fun getMode(): DcMotor.RunMode = mode
}