package com.example.celestic.ui.screen


import android.content.Context
import android.util.Log
import org.json.JSONObject
import org.opencv.core.CvType
import org.opencv.core.Mat
import java.io.File
import java.io.FileInputStream

object CalibrationManager {

    var cameraMatrix: Mat? = null
    var distortionCoeffs: Mat? = null
    var resolution: Pair<Int, Int>? = null
    var calibrationDate: String? = null

    private const val calibrationPath = "/storage/emulated/0/Celestic/config/calibration.json"

    fun loadCalibration(context: Context): Boolean {
        return try {
            val file = File(calibrationPath)
            val json = JSONObject(FileInputStream(file).bufferedReader().use { it.readText() })

            // Parse cameraMatrix
            val matrixArray = json.getJSONArray("cameraMatrix")
            val matrix = Mat(3, 3, CvType.CV_64F)
            for (i in 0 until 3) {
                val row = matrixArray.getJSONArray(i)
                for (j in 0 until 3) {
                    matrix.put(i, j, row.getDouble(j))
                }
            }

            // Parse distortionCoeffs
            val coeffsArray = json.getJSONArray("distortionCoeffs")
            val coeffs = Mat(1, coeffsArray.length(), CvType.CV_64F)
            for (i in 0 until coeffsArray.length()) {
                coeffs.put(0, i, coeffsArray.getDouble(i))
            }

            // Resolution
            val resArray = json.getJSONArray("resolution")
            resolution = Pair(resArray.getInt(0), resArray.getInt(1))

            // Metadata
            calibrationDate = json.getString("calibrationDate")

            cameraMatrix = matrix
            distortionCoeffs = coeffs

            true
        } catch (e: Exception) {
            Log.e("CalibrationManager", "Error al cargar calibración", e)
            false
        }
    }

    fun getScaleFactor(pixelLength: Double): Double {
        // Ejemplo simple: escalar 1px ≈ X mm (reemplazar con lógica real si tienes datos de referencia)
        val focalLength = cameraMatrix?.get(0, 0)?.firstOrNull() ?: return 0.0
        val mmPerPixel = 1.0 / focalLength // Sujeto a corrección según distancia focal real
        return pixelLength * mmPerPixel
    }
}