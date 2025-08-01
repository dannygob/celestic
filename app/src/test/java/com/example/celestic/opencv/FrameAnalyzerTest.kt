package com.example.celestic.opencv

import com.example.celestic.viewmodel.SharedViewModel
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.opencv.core.Mat

class FrameAnalyzerTest {

    private lateinit var frameAnalyzer: FrameAnalyzer
    private lateinit var sharedViewModel: SharedViewModel

    @Before
    fun setUp() {
        sharedViewModel = mockk(relaxed = true)
        frameAnalyzer = FrameAnalyzer(sharedViewModel)
    }

    @Test
    fun `analyze should return an analysis result`() {
        val mat = mockk<Mat>(relaxed = true)
        val result = frameAnalyzer.analyze(mat)
        assert(result.contours.isEmpty())
    }
}
