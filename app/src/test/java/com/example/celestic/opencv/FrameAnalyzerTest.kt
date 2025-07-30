package com.example.celestic.opencv

import com.example.celestic.ui.screen.FrameAnalyzer
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.opencv.core.Mat

class FrameAnalyzerTest {

    private lateinit var frameAnalyzer: FrameAnalyzer

    @Before
    fun setUp() {
        frameAnalyzer = FrameAnalyzer()
    }

    @Test
    fun `analyze should return an analysis result`() {
        val mat = mockk<Mat>()
        val result = frameAnalyzer.analyze(mat)
        assert(result.contours.isEmpty())
    }
}
