package com.example.celestic.utils

import android.content.Context
import com.example.celestic.models.DetectionItem
import com.google.gson.Gson
import java.io.File
import java.io.FileWriter

fun generatePdfFromDetections(context: Context, detections: List<DetectionItem>, loteId: String): File? {
    // TODO: Implement PDF generation
    return null
}

fun generateWordFromDetections(context: Context, detections: List<DetectionItem>, loteId: String): File? {
    // TODO: Implement Word generation
    return null
}

fun exportJsonSummary(context: Context, detections: List<DetectionItem>, loteId: String): File? {
    val gson = Gson()
    val json = gson.toJson(detections)
    val file = File(context.getExternalFilesDir(null), "ReporteCelestic_$loteId.json")
    FileWriter(file).use { it.write(json) }
    return file
}
