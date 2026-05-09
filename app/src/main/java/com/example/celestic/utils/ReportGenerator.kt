package com.example.celestic.utils

import android.content.Context
import android.os.Environment
import com.example.celestic.R
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

/**
 * Creates or retrieves the 'Celestic/Reports' directory in the device's public Documents folder.
 * Ensures the directory exists before returning it.
 *
 * @param context Application or Activity context.
 * @return File object representing the reports directory.
 */
private fun getReportDirectory(context: Context): File {
    val dir = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
        "Celestic/Reports"
    )
    if (!dir.exists()) {
        dir.mkdirs()
    }
    return dir
}

/**
 * Generates a dynamic filename for the report based on the current date and batch (Albarán).
 * If the report is a general shift report, it uses a standard name. If it's a failure report,
 * it appends a sequential 'FAIL_XXX' counter.
 *
 * @param context Context to access SharedPreferences.
 * @param extension File extension (e.g., "pdf", "csv").
 * @param isGeneral True if this is a general report, false if it is a failure report.
 * @return Formatted filename string.
 */
private fun getReportFilename(context: Context, extension: String, isGeneral: Boolean): String {
    val sharedPrefs = context.getSharedPreferences("celestic_prefs", Context.MODE_PRIVATE)
    val albaran = sharedPrefs.getString("current_albaran", "GENERAL") ?: "GENERAL"
    val dateStr = java.text.SimpleDateFormat("ddMMyyyy").format(java.util.Date())

    return if (isGeneral) {
        "REP_${dateStr}_${albaran}.$extension"
    } else {
        val key = "fail_count_${dateStr}_$albaran"
        val count = sharedPrefs.getInt(key, 0) + 1
        sharedPrefs.edit().putInt(key, count).apply()
        val serial = String.format("%03d", count)
        "REP_${dateStr}_${albaran}_FAIL_$serial.$extension"
    }
}

/**
 * Generates a PDF report containing the details of the given detections.
 * Includes metadata such as Operator, Shift, Batch Number, and Date.
 *
 * @param context Context to access preferences and resources.
 * @param detections List of DetectionItems to include in the report.
 * @param isGeneral Flag to determine if it's a general or failure report.
 * @return File object pointing to the newly created PDF.
 */
fun generatePdfFromDetections(
    context: Context,
    detections: List<DetectionItem>,
    isGeneral: Boolean = false
): File {
    val filename = getReportFilename(context, "pdf", isGeneral)
    val file = File(getReportDirectory(context), filename)
    val writer = PdfWriter(file)
    val pdf = PdfDocument(writer)
    val document = Document(pdf)

    val sharedPrefs = context.getSharedPreferences("celestic_prefs", Context.MODE_PRIVATE)
    val operator =
        sharedPrefs.getString("current_user", context.getString(R.string.unknownOperator))
    val shift = sharedPrefs.getString("current_shift", context.getString(R.string.unassignedShift))
    val albaran =
        sharedPrefs.getString("current_albaran", context.getString(R.string.generalAlbaran))
    val date = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(java.util.Date())

    document.add(
        Paragraph(context.getString(R.string.reportHeader, albaran)).setBold().setFontSize(18f)
    )
    document.add(Paragraph(context.getString(R.string.operatorLabel, operator)))
    document.add(Paragraph(context.getString(R.string.shiftLabel, shift)))
    document.add(Paragraph(context.getString(R.string.dateLabel, date)))
    document.add(Paragraph(" "))
    
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

/**
 * Generates a CSV (Comma Separated Values) report containing the details of the given detections.
 * Ideal for importing data into databases or raw excel processing.
 *
 * @param context Context to access preferences and resources.
 * @param detections List of DetectionItems.
 * @param isGeneral Flag to determine if it's a general or failure report.
 * @return File object pointing to the newly created CSV.
 */
fun generateCsvFromDetections(
    context: Context,
    detections: List<DetectionItem>,
    isGeneral: Boolean = false
): File {
    val filename = getReportFilename(context, "csv", isGeneral)
    val file = File(getReportDirectory(context), filename)
    val writer = file.bufferedWriter()
    val sharedPrefs = context.getSharedPreferences("celestic_prefs", Context.MODE_PRIVATE)
    val operator =
        sharedPrefs.getString("current_user", context.getString(R.string.unknownOperator))
    val shift = sharedPrefs.getString("current_shift", context.getString(R.string.unassignedShift))
    val albaran =
        sharedPrefs.getString("current_albaran", context.getString(R.string.generalAlbaran))
    val date = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(java.util.Date())

    writer.write(
        "${context.getString(R.string.albaranNumber)}: $albaran, ${
            context.getString(
                R.string.operatorLabel,
                operator
            )
        }, ${context.getString(R.string.shiftLabel, shift)}, ${
            context.getString(
                R.string.dateLabel,
                date
            )
        }\n\n"
    )
    writer.write("ID,Tipo,Confianza,Status,Ancho (mm),Alto (mm)\n")
    detections.forEach {
        val width = it.measurementMm
        val height = it.measurementMm
        writer.write("${it.id},${it.type},${it.confidence},${it.status},${width ?: ""},${height ?: ""}\n")
    }
    writer.close()
    return file
}

/**
 * Generates a Microsoft Word (.docx) report with the inspection data.
 * Formats the text into paragraphs for easy reading and printing.
 *
 * @param context Context to access preferences and resources.
 * @param detections List of DetectionItems.
 * @param isGeneral Flag to determine if it's a general or failure report.
 * @return File object pointing to the newly created Word document.
 */
fun generateWordFromDetections(
    context: Context,
    detections: List<DetectionItem>,
    isGeneral: Boolean = false
): File {
    val filename = getReportFilename(context, "docx", isGeneral)
    val file = File(getReportDirectory(context), filename)
    val document = org.apache.poi.xwpf.usermodel.XWPFDocument()

    val title = document.createParagraph()
    val titleRun = title.createRun()
    titleRun.setBold(true)
    titleRun.fontSize = 20

    val sharedPrefs = context.getSharedPreferences("celestic_prefs", Context.MODE_PRIVATE)
    val albaran =
        sharedPrefs.getString("current_albaran", context.getString(R.string.generalAlbaran))
    titleRun.setText(context.getString(R.string.reportHeader, albaran))

    val operator =
        sharedPrefs.getString("current_user", context.getString(R.string.unknownOperator))
    val shift = sharedPrefs.getString("current_shift", context.getString(R.string.unassignedShift))
    val date = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(java.util.Date())

    val meta = document.createParagraph()
    val metaRun = meta.createRun()
    metaRun.setText(
        "${
            context.getString(
                R.string.operatorLabel,
                operator
            )
        } | ${context.getString(R.string.shiftLabel, shift)} | ${
            context.getString(
                R.string.dateLabel,
                date
            )
        }"
    )
    metaRun.addBreak()

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

/**
 * Generates a Microsoft Excel (.xlsx) report.
 * Creates a structured table with headers and styled cells for data analysis.
 *
 * @param context Context to access preferences and resources.
 * @param detections List of DetectionItems.
 * @param isGeneral Flag to determine if it's a general or failure report.
 * @return File object pointing to the newly created Excel workbook.
 */
fun generateExcelFromDetections(
    context: Context,
    detections: List<DetectionItem>,
    isGeneral: Boolean = false
): File {
    val filename = getReportFilename(context, "xlsx", isGeneral)
    val file = File(getReportDirectory(context), filename)
    val workbook = XSSFWorkbook()
    val sheet = workbook.createSheet("Detecciones")

    val sharedPrefs = context.getSharedPreferences("celestic_prefs", Context.MODE_PRIVATE)
    val operator =
        sharedPrefs.getString("current_user", context.getString(R.string.unknownOperator))
    val shift = sharedPrefs.getString("current_shift", context.getString(R.string.unassignedShift))
    val albaran =
        sharedPrefs.getString("current_albaran", context.getString(R.string.generalAlbaran))
    val date = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(java.util.Date())

    val metaRow = sheet.createRow(0)
    metaRow.createCell(0)
        .setCellValue(
            "${context.getString(R.string.albaranNumber)}: $albaran | ${
                context.getString(
                    R.string.operatorLabel,
                    operator
                )
            } | ${
                context.getString(
                    R.string.shiftLabel,
                    shift
                )
            } | ${context.getString(R.string.dateLabel, date)}"
        )
    
    // Header
    val headerRow = sheet.createRow(2)
    val headerStyle = workbook.createCellStyle()
    headerStyle.fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
    headerStyle.fillPattern = FillPatternType.SOLID_FOREGROUND
    val font = workbook.createFont()
    font.bold = true
    headerStyle.setFont(font)

    val headers = listOf("ID", "Tipo", "Confianza", "Status", "Medida (mm)")
    headers.forEachIndexed { index, title ->
        val cell = headerRow.createCell(index)
        val cellObj = cell
        cellObj.setCellValue(title)
        cellObj.cellStyle = headerStyle
    }

    // Data
    detections.forEachIndexed { index, item ->
        val row = sheet.createRow(index + 3)
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

/**
 * Exports the raw detection data directly into a JSON file.
 * Useful for machine-to-machine communication or system backups.
 *
 * @param context Context to access preferences.
 * @param detections List of DetectionItems.
 * @param isGeneral Flag to determine if it's a general or failure report.
 * @return File object pointing to the JSON file.
 */
fun exportJsonSummary(
    context: Context,
    detections: List<DetectionItem>,
    isGeneral: Boolean = false
): File {
    val gson = Gson()
    val json = gson.toJson(detections)
    val filename = getReportFilename(context, "json", isGeneral)
    val file = File(getReportDirectory(context), filename)
    FileWriter(file).use { it.write(json) }
    return file
}
