package com.asiankoala.koawalib.subsystem.vision

import com.asiankoala.koawalib.hardware.KDevice
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName
import org.openftc.easyopencv.OpenCvCamera
import org.openftc.easyopencv.OpenCvCameraFactory
import org.openftc.easyopencv.OpenCvCameraRotation
import org.openftc.easyopencv.OpenCvPipeline

/**
 * Webcam wrapper for [OpenCvPipeline]
 * @param[deviceName] Hardware configuration name for the webcam
 * @param[pipeline] The [OpenCvPipeline] that will be run
 * @param[width] Resolution width. Default 800
 * @param[height] Resolution height. Default 448
 * @param[orientation] Camera orientation. Default
 */
class KWebcam(
    deviceName: String,
    pipeline: OpenCvPipeline,
    private val width: Int,
    private val height: Int,
    private val orientation: OpenCvCameraRotation
) : KDevice<WebcamName>(deviceName) {
    private val camera: OpenCvCamera

    /**
     * Start the camera stream
     */
    fun startStreaming() {
        camera.openCameraDeviceAsync(object : OpenCvCamera.AsyncCameraOpenListener {
            override fun onOpened() {
                camera.startStreaming(width, height, orientation)
            }

            override fun onError(errorCode: Int) { }
        })
    }

    /**
     * Stop the camera stream
     */
    fun stopStreaming() {
        camera.stopStreaming()
    }

    init {
        val cameraMonitorViewId = hardwareMap.appContext.resources.getIdentifier(
            "cameraMonitorViewId",
            "id",
            hardwareMap.appContext.packageName
        )
        camera = OpenCvCameraFactory.getInstance().createWebcam(device, cameraMonitorViewId)
        camera.setPipeline(pipeline)
    }
}
