package com.example.celestic.utils

import android.content.Context
import com.example.celestic.models.DetectionItem
import com.google.gson.Gson
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter

fun generatePdfFromDetections(context: Context, detections: List<DetectionItem>, loteId: String): File {
    val file = File(context.getExternalFilesDir(null), "ReporteCelestic_$loteId.pdf")
    val writer = PdfWriter(file)
    val pdf = PdfDocument(writer)
    val document = Document(pdf)

    document.add(Paragraph("Reporte de Detecciones - Lote: $loteId"))
    detections.forEach {
        document.add(Paragraph("ID: ${it.id}"))
        document.add(Paragraph("Tipo: ${it.type}"))
        document.add(Paragraph("Confianza: ${it.confidence}"))
        document.add(Paragraph("Status: ${it.status}"))
        document.add(Paragraph("--------------------"))
    }

    document.close()
    return file
}

fun generateCsvFromDetections(context: Context, detections: List<DetectionItem>, loteId: String): File {
    val file = File(context.getExternalFilesDir(null), "ReporteCelestic_$loteId.csv")
    val writer = file.bufferedWriter()
    writer.write("ID,Tipo,Confianza,Status,Ancho (mm),Alto (mm)\n")
    detections.forEach {
        val width = it.measurementMm
        val height = it.measurementMm
        writer.write("${it.id},${it.type},${it.confidence},${it.status},${width ?: ""},${height ?: ""}\n")
    }
    writer.close()
    return file
}

fun generateWordFromDetections(context: Context, detections: List<DetectionItem>, loteId: String): File {
    val file = File(context.getExternalFilesDir(null), "ReporteCelestic_$loteId.docx")
    val document = org.apache.poi.xwpf.usermodel.XWPFDocument()

    val title = document.createParagraph()
    val titleRun = title.createRun()
    titleRun.setBold(true)
    titleRun.fontSize = 20
    titleRun.setText("Reporte de Detecciones - Lote: $loteId")

    detections.forEach {
        val paragraph = document.createParagraph()
        val run = paragraph.createRun()
        run.setText("ID: ${it.id} | Tipo: ${it.type} | Confianza: ${it.confidence} | Status: ${it.status}")
    }

    val fileOut = FileOutputStream(file)
    document.write(fileOut)
    fileOut.close()
    return file
}

fun generateExcelFromDetections(context: Context, detections: List<DetectionItem>, loteId: String): File {
    val file = File(context.getExternalFilesDir(null), "ReporteCelestic_$loteId.xlsx")
    val workbook = XSSFWorkbook()
    val sheet = workbook.createSheet("Detecciones")

    // Header
    val headerRow = sheet.createRow(0)
    val headerStyle = workbook.createCellStyle()
    headerStyle.fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
    headerStyle.fillPattern = FillPatternType.SOLID_FOREGROUND
    val font = workbook.createFont()
    font.bold = true
    headerStyle.setFont(font)

    val headers = listOf("ID", "Tipo", "Confianza", "Status", "Medida (mm)")
    headers.forEachIndexed { index, title ->
        val cell = headerRow.createCell(index)
        cell.setCellValue(title)
        cell.cellStyle = headerStyle
    }

    // Data
    detections.forEachIndexed { index, item ->
        val row = sheet.createRow(index + 1)
        row.createCell(0).setCellValue(item.id.toDouble())
        row.createCell(1).setCellValue(item.type.toString())
        row.createCell(2).setCellValue(item.confidence.toDouble())
        row.createCell(3).setCellValue(item.status.toString())
        row.createCell(4).setCellValue(item.measurementMm?.toDouble() ?: 0.0)
    }

    for (i in headers.indices) {
        sheet.autoSizeColumn(i)
    }

    val fileOut = FileOutputStream(file)
    workbook.write(fileOut)
    fileOut.close()
    workbook.close()
    return file
}

fun exportJsonSummary(context: Context, detections: List<DetectionItem>, loteId: String): File {
    val gson = Gson()
    val json = gson.toJson(detections)
    val file = File(context.getExternalFilesDir(null), "ReporteCelestic_$loteId.json")
    FileWriter(file).use { it.write(json) }
    return file
}
