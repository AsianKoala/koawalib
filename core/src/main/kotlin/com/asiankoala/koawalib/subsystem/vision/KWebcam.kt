package com.asiankoala.koawalib.subsystem.vision

import com.asiankoala.koawalib.hardware.KDevice
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName
import org.openftc.easyopencv.OpenCvCamera
import org.openftc.easyopencv.OpenCvCameraFactory
import org.openftc.easyopencv.OpenCvCameraRotation
import org.openftc.easyopencv.OpenCvPipeline

class Webcam(
    deviceName: String,
    pipeline: OpenCvPipeline,
    private val width: Int = 800,
    private val height: Int = 448,
    private val orientation: OpenCvCameraRotation = OpenCvCameraRotation.UPRIGHT
) : KDevice<WebcamName>(deviceName) {
    private val camera: OpenCvCamera

    fun startStreaming() {
        camera.openCameraDeviceAsync(object : OpenCvCamera.AsyncCameraOpenListener {
            override fun onOpened() {
                camera.startStreaming(width, height, orientation)
            }

            override fun onError(errorCode: Int) { }
        })
    }

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
