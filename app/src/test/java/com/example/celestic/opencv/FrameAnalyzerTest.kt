package com.example.celestic.opencv

import com.example.celestic.manager.AprilTagManager
import com.example.celestic.manager.ArUcoManager
import com.example.celestic.manager.CalibrationManager
import io.mockk.mockk
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.opencv.core.Mat

class FrameAnalyzerTest {

    private lateinit var frameAnalyzer: FrameAnalyzer
    private lateinit var arucoManager: ArUcoManager
    private lateinit var aprilTagManager: AprilTagManager
    private lateinit var calibrationManager: CalibrationManager

    @Before
    fun setUp() {
        arucoManager = mockk(relaxed = true)
        aprilTagManager = mockk(relaxed = true)
        calibrationManager = mockk(relaxed = true)
        frameAnalyzer = FrameAnalyzer(arucoManager, aprilTagManager, calibrationManager)
    }

    @Test
    @Ignore("Requiere la inicialización de la librería nativa de OpenCV JNI en un entorno Android (ejecutar como Android Instrumented Test).")
    fun `analyze should return an analysis result`() {
        val mat = mockk<Mat>(relaxed = true)
        val result = frameAnalyzer.analyze(mat, null)
        assert(result.contours.isEmpty())
    }
}
