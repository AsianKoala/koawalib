package com.asiankoala.koawalib.hardware.sensor

import com.qualcomm.hardware.bosch.BNO055IMU
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder

object IMUUtil {
    /**
     * Remap BNO055 IMU axes and signs. For reference, the default order is [AxesOrder.ZYX].
     * Call after [BNO055IMU.initialize].
     *
     *
     * Adapted from [this post](https://ftcforum.usfirst.org/forum/ftc-technology/53812-mounting-the-revhub-vertically?p=56587#post56587).
     *
     * @param imu   IMU
     * @param order axes order
     * @param signs axes signs
     */
    fun remapAxes(imu: BNO055IMU, order: AxesOrder, signs: AxesSigns) {
        try {
            // the indices correspond with the 2-bit encodings specified in the datasheet
            val indices = order.indices()
            var axisMapConfig = 0
            axisMapConfig = axisMapConfig or (indices[0] shl 4)
            axisMapConfig = axisMapConfig or (indices[1] shl 2)
            axisMapConfig = axisMapConfig or (indices[2] shl 0)

            // the BNO055 driver flips the first orientation vector so we also flip here
            val axisMapSign = signs.bVal xor (4 shr indices[0])

            // Enter CONFIG mode
            imu.write8(BNO055IMU.Register.OPR_MODE, BNO055IMU.SensorMode.CONFIG.bVal.toInt() and 0x0F)
            Thread.sleep(100)

            // Write the AXIS_MAP_CONFIG register
            imu.write8(BNO055IMU.Register.AXIS_MAP_CONFIG, axisMapConfig and 0x3F)

            // Write the AXIS_MAP_SIGN register
            imu.write8(BNO055IMU.Register.AXIS_MAP_SIGN, axisMapSign and 0x07)

            // Switch back to the previous mode
            imu.write8(BNO055IMU.Register.OPR_MODE, imu.parameters.mode.bVal.toInt() and 0x0F)
            Thread.sleep(100)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}
