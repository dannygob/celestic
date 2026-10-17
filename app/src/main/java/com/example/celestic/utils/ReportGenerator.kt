package com.example.celestic.utils

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import androidx.core.content.edit
import com.example.celestic.R
import com.example.celestic.models.DetectionItem
import com.example.celestic.models.enums.DetectionStatus
import com.google.gson.GsonBuilder
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter

// ─────────────────────────────────────────────────────────────────────────────
// REPORT MODE
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Defines the type of report to generate.
 *
 * - [BATCH_APPROVAL]   → Summary report for an entire approved lot.
 *                        Shows global pass/fail counts and confirms the batch
 *                        meets quality requirements.
 *
 * - [PIECE_REJECTION]  → Individual Non-Conformance Report (NCR) for a single
 *                        rejected piece. Includes the full defect blueprint:
 *                        detection type, measurements, AI notes, and reason for
 *                        rejection so the operator/quality team can trace back
 *                        exactly what failed and why.
 */
enum class ReportMode {
    BATCH_APPROVAL,
    PIECE_REJECTION
}

// ─────────────────────────────────────────────────────────────────────────────
// INTERNAL HELPERS
// ─────────────────────────────────────────────────────────────────────────────

private fun getReportDirectory(context: Context): File {
    val dir = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
        "Celestic/Reports"
    )
    if (!dir.exists()) dir.mkdirs()
    return dir
}

private fun getReportFilename(
    context: Context,
    extension: String,
    mode: ReportMode
): String {
    val prefs = context.getSharedPreferences("celestic_prefs", Context.MODE_PRIVATE)
    val albaran = prefs.getString("current_albaran", "GENERAL") ?: "GENERAL"
    val dateStr = java.text.SimpleDateFormat("ddMMyyyy").format(java.util.Date())

    return when (mode) {
        ReportMode.BATCH_APPROVAL ->
            "APR_${dateStr}_${albaran}.$extension"

        ReportMode.PIECE_REJECTION -> {
            val key = "ncr_count_${dateStr}_$albaran"
            val count = prefs.getInt(key, 0) + 1
            prefs.edit { putInt(key, count) }
            "NCR_${dateStr}_${albaran}_${"${count}".padStart(3, '0')}.$extension"
        }
    }
}

/** Reads common header and company metadata from SharedPreferences. */
private data class ReportMeta(
    val albaran:    String,
    val operator:   String,
    val shift:      String,
    val date:       String,
    // Company fields
    val companyName:   String,
    val companyAddr:   String,
    val companyPhone:  String,
    val companyVat:    String,
    val qualityMgr:    String
)

private fun loadMeta(context: Context): ReportMeta {
    val prefs = context.getSharedPreferences("celestic_prefs", Context.MODE_PRIVATE)
    return ReportMeta(
        albaran      = prefs.getString("current_albaran", context.getString(R.string.general_batch)) ?: "",
        operator     = prefs.getString("current_user",    context.getString(R.string.unknown_operator)) ?: "",
        shift        = prefs.getString("current_shift",   context.getString(R.string.unassigned_shift)) ?: "",
        date         = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(java.util.Date()),
        companyName  = prefs.getString("company_name",        "—") ?: "—",
        companyAddr  = prefs.getString("company_address",     "—") ?: "—",
        companyPhone = prefs.getString("company_phone",       "—") ?: "—",
        companyVat   = prefs.getString("company_vat",         "—") ?: "—",
        qualityMgr   = prefs.getString("company_quality_mgr", "—") ?: "—"
    )
}

/** Converts a Bitmap to a JPEG byte array for iText embedding. */
private fun Bitmap.toJpegBytes(quality: Int = 88): ByteArray {
    val bos = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, quality, bos)
    return bos.toByteArray()
}

// ─────────────────────────────────────────────────────────────────────────────
// PDF
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Generates a PDF report.
 *
 * - [ReportMode.BATCH_APPROVAL]  → One-page summary: lot totals, pass rate,
 *                                   and a consolidated list of all inspected pieces.
 * - [ReportMode.PIECE_REJECTION] → Full Non-Conformance Report (NCR) with the
 *                                   complete defect blueprint: type, measurements,
 *                                   bounding-box dimensions, AI/OpenCV diagnosis
 *                                   notes, and reason for rejection.
 */
fun generatePdfFromDetections(
    context: Context,
    detections: List<DetectionItem>,
    isGeneral: Boolean = false          // kept for backwards compatibility
): File {
    val mode = if (isGeneral) ReportMode.BATCH_APPROVAL else ReportMode.PIECE_REJECTION
    return generatePdfReport(context, detections, mode)
}

fun generatePdfReport(
    context: Context,
    detections: List<DetectionItem>,
    mode: ReportMode
): File {
    val meta     = loadMeta(context)
    val filename = getReportFilename(context, "pdf", mode)
    val file     = File(getReportDirectory(context), filename)
    val document = Document(PdfDocument(PdfWriter(file)))

    val accentColor  = DeviceRgb(0, 102, 204)     // corporate blue
    val dangerColor  = DeviceRgb(204, 0, 0)        // rejection red
    val successColor = DeviceRgb(0, 128, 64)       // approval green
    val headerGray   = DeviceRgb(240, 240, 240)

    // ── COMPANY HEADER (shared between both modes) ────────────────────────────
    val headerTable = Table(UnitValue.createPercentArray(floatArrayOf(70f, 30f))).useAllAvailableWidth()
    headerTable.addCell(
        Cell().add(Paragraph(meta.companyName).setBold().setFontSize(16f))
            .setBorder(com.itextpdf.layout.borders.SolidBorder(accentColor, 0f))
            .setPaddingBottom(2f)
    )
    headerTable.addCell(
        Cell().add(
            Paragraph("CELESTIC QC SYSTEM\n${meta.date}")
                .setTextAlignment(TextAlignment.RIGHT).setFontSize(9f).setFontColor(ColorConstants.GRAY)
        ).setBorder(com.itextpdf.layout.borders.SolidBorder(accentColor, 0f))
    )
    headerTable.addCell(
        Cell(1, 2).add(
            Paragraph("${meta.companyAddr}  |  Tel: ${meta.companyPhone}  |  NIF/CIF: ${meta.companyVat}")
                .setFontSize(9f).setFontColor(ColorConstants.GRAY)
        ).setBorder(com.itextpdf.layout.borders.SolidBorder(accentColor, 0f)).setPaddingBottom(4f)
    )
    document.add(headerTable)

    // Blue separator line
    document.add(
        Table(UnitValue.createPercentArray(floatArrayOf(100f))).useAllAvailableWidth().apply {
            addCell(Cell().setHeight(3f).setBackgroundColor(accentColor)
                .setBorder(com.itextpdf.layout.borders.SolidBorder(accentColor, 0f)))
        }
    )
    document.add(Paragraph(" "))

    when (mode) {

        // ── BATCH APPROVAL ────────────────────────────────────────────────────
        ReportMode.BATCH_APPROVAL -> {
            val approved = detections.count { it.status == DetectionStatus.OK }
            val warnings = detections.count { it.status == DetectionStatus.WARNING }
            val rejected = detections.count { it.status == DetectionStatus.NOT_ACCEPTED }
            val total    = detections.size
            val passRate = if (total > 0) approved * 100 / total else 0
            val verdict =
                if (rejected == 0) context.getString(R.string.report_verdict_approved) else context.getString(
                    R.string.report_verdict_nonconforming
                )
            val vColor   = if (rejected == 0) successColor else dangerColor

            document.add(
                Paragraph(context.getString(R.string.report_cert_title))
                    .setBold().setFontSize(18f).setFontColor(accentColor)
            )
            document.add(Paragraph(" "))

            // Metadata table
            val infoTable = Table(UnitValue.createPercentArray(floatArrayOf(25f, 75f))).useAllAvailableWidth()
            listOf(
                context.getString(R.string.report_field_batch) to meta.albaran,
                context.getString(R.string.report_field_operator) to meta.operator,
                context.getString(R.string.report_field_shift) to meta.shift,
                context.getString(R.string.report_field_date) to meta.date,
                context.getString(R.string.report_field_quality_mgr) to meta.qualityMgr
            ).forEach { (label, value) ->
                infoTable.addCell(Cell().add(Paragraph(label).setBold().setFontSize(10f))
                    .setBackgroundColor(headerGray))
                infoTable.addCell(Cell().add(Paragraph(value).setFontSize(10f)))
            }
            document.add(infoTable)
            document.add(Paragraph(" "))

            // Summary totals
            document.add(
                Paragraph(context.getString(R.string.report_summary_header)).setBold()
                    .setFontSize(13f).setFontColor(accentColor)
            )
            val summaryTable = Table(UnitValue.createPercentArray(floatArrayOf(50f, 50f))).useAllAvailableWidth()
            listOf(
                context.getString(R.string.report_total_parts) to total.toString(),
                "✔ " + context.getString(R.string.report_approved_count) to approved.toString(),
                "⚠ " + context.getString(R.string.report_warning_count) to warnings.toString(),
                "✖ " + context.getString(R.string.report_rejected_count) to rejected.toString(),
                context.getString(R.string.report_pass_rate) to "$passRate %"
            ).forEach { (k, v) ->
                summaryTable.addCell(Cell().add(Paragraph(k).setBold().setFontSize(11f)).setBackgroundColor(headerGray))
                summaryTable.addCell(Cell().add(Paragraph(v).setFontSize(11f)))
            }
            document.add(summaryTable)
            document.add(Paragraph(" "))

            // Verdict stamp
            document.add(
                Paragraph(verdict).setBold().setFontSize(16f).setFontColor(vColor)
                    .setTextAlignment(TextAlignment.CENTER)
            )
            document.add(Paragraph(" "))

            // Detail table
            document.add(
                Paragraph(context.getString(R.string.report_piece_detail_header)).setBold()
                    .setFontSize(12f).setFontColor(accentColor)
            )
            val detailTable = Table(UnitValue.createPercentArray(floatArrayOf(8f, 22f, 15f, 20f, 18f, 17f))).useAllAvailableWidth()
            listOf("ID", "Tipo", "Confianza", "Status", "Medida (mm)", "Notas").forEach { h ->
                detailTable.addHeaderCell(Cell().add(Paragraph(h).setBold().setFontSize(9f)).setBackgroundColor(accentColor).setFontColor(ColorConstants.WHITE))
            }
            detections.forEach { item ->
                val rowColor = when (item.status) {
                    DetectionStatus.OK           -> DeviceRgb(230, 255, 230)
                    DetectionStatus.WARNING      -> DeviceRgb(255, 248, 220)
                    DetectionStatus.NOT_ACCEPTED -> DeviceRgb(255, 235, 235)
                }
                listOf(
                    item.id.toString(),
                    item.type.toString(),
                    "%.1f%%".format(item.confidence * 100),
                    item.status.toString(),
                    item.measurementMm?.let { "%.2f".format(it) } ?: "N/A",
                    item.notes.take(60).ifBlank { "—" }
                ).forEach { v ->
                    detailTable.addCell(Cell().add(Paragraph(v).setFontSize(8f)).setBackgroundColor(rowColor))
                }
            }
            document.add(detailTable)
            document.add(Paragraph(" "))

            // Signature block
            document.add(
                Paragraph(context.getString(R.string.report_signatures))
                .setFontSize(10f).setTextAlignment(TextAlignment.CENTER))
        }

        // ── PIECE REJECTION (NCR) ─────────────────────────────────────────────
        ReportMode.PIECE_REJECTION -> {
            document.add(
                Paragraph(context.getString(R.string.report_ncr_title))
                    .setBold().setFontSize(18f).setFontColor(dangerColor)
            )
            document.add(Paragraph(" "))

            // NCR metadata table
            val ncrMeta = Table(UnitValue.createPercentArray(floatArrayOf(25f, 75f))).useAllAvailableWidth()
            listOf(
                context.getString(R.string.report_field_company) to meta.companyName,
                context.getString(R.string.report_field_address) to meta.companyAddr,
                context.getString(R.string.report_field_phone) to meta.companyPhone,
                context.getString(R.string.report_field_vat) to meta.companyVat,
                context.getString(R.string.report_field_batch) to meta.albaran,
                context.getString(R.string.report_field_operator) to meta.operator,
                context.getString(R.string.report_field_shift) to meta.shift,
                context.getString(R.string.report_field_date) to meta.date,
                context.getString(R.string.report_field_quality_mgr) to meta.qualityMgr
            ).forEach { (label, value) ->
                ncrMeta.addCell(Cell().add(Paragraph(label).setBold().setFontSize(10f)).setBackgroundColor(headerGray))
                ncrMeta.addCell(Cell().add(Paragraph(value).setFontSize(10f)))
            }
            document.add(ncrMeta)
            document.add(Paragraph(" "))

            // Per-detection NCR blocks
            detections.forEach { item ->
                val medida  = item.measurementMm?.let { "%.2f mm".format(it) } ?: "N/A"
                val conf    = "%.1f%%".format(item.confidence * 100)
                val bbox    = item.boundingBox

                document.add(
                    Paragraph(
                        context.getString(
                            R.string.report_defect_header,
                            item.id,
                            item.type.toString()
                        )
                    )
                        .setBold().setFontSize(13f).setFontColor(dangerColor)
                )

                val defectTable = Table(UnitValue.createPercentArray(floatArrayOf(35f, 65f))).useAllAvailableWidth()
                listOf(
                    context.getString(R.string.report_defect_type) to item.type.toString(),
                    context.getString(R.string.report_status) to item.status.toString(),
                    context.getString(R.string.report_ai_confidence) to conf,
                    context.getString(R.string.report_calibrated_measure) to medida,
                    context.getString(R.string.report_affected_area) to "%.0f, %.0f, %.0f, %.0f px (%.0f×%.0f)".format(
                        bbox.left, bbox.top, bbox.right, bbox.bottom, bbox.width(), bbox.height())
                ).forEach { (k, v) ->
                    defectTable.addCell(Cell().add(Paragraph(k).setBold().setFontSize(10f)).setBackgroundColor(headerGray))
                    defectTable.addCell(Cell().add(Paragraph(v).setFontSize(10f)))
                }
                document.add(defectTable)

                // Diagnosis / Blueprint
                if (item.notes.isNotBlank()) {
                    document.add(Paragraph(" "))
                    document.add(
                        Paragraph(context.getString(R.string.report_diagnosis_header)).setBold()
                            .setFontSize(11f).setFontColor(dangerColor)
                    )
                    val diagTable = Table(UnitValue.createPercentArray(floatArrayOf(100f))).useAllAvailableWidth()
                    item.notes.split("|").map { it.trim() }.filter { it.isNotBlank() }.forEach { line ->
                        diagTable.addCell(Cell().add(Paragraph("• $line").setFontSize(10f)).setBackgroundColor(DeviceRgb(255, 245, 245)))
                    }
                    document.add(diagTable)
                }

                // ── Annotated frame image ──────────────────────────────────────
                document.add(Paragraph(" "))
                val annotated = ReportAnnotator.annotateFrame(context, item.frameId, listOf(item))
                if (annotated != null) {
                    document.add(
                        Paragraph(context.getString(R.string.report_image_header)).setBold()
                            .setFontSize(10f)
                    )
                    try {
                        val imgData  = ImageDataFactory.create(annotated.toJpegBytes())
                        val pdfImage = Image(imgData).setAutoScale(true)
                        document.add(pdfImage)
                    } catch (_: Exception) { /* frame unreadable — skip image */ }
                    annotated.recycle()
                }

                document.add(Paragraph(" "))

                // Signature block
                val sigTable = Table(UnitValue.createPercentArray(floatArrayOf(50f, 50f))).useAllAvailableWidth()
                sigTable.addCell(
                    Cell().add(
                        Paragraph("${context.getString(R.string.report_field_operator)}:\n\n_______________________").setFontSize(
                            10f
                        )
                    )
                )
                sigTable.addCell(
                    Cell().add(
                        Paragraph("${context.getString(R.string.report_field_quality_mgr)}:\n\n_______________________").setFontSize(
                            10f
                        )
                    )
                )
                document.add(sigTable)
                document.add(Paragraph(" "))
            }
        }
    }

    document.close()
    return file
}

// ─────────────────────────────────────────────────────────────────────────────
// CSV
// ─────────────────────────────────────────────────────────────────────────────

fun generateCsvFromDetections(
    context: Context,
    detections: List<DetectionItem>,
    isGeneral: Boolean = false
): File {
    val mode     = if (isGeneral) ReportMode.BATCH_APPROVAL else ReportMode.PIECE_REJECTION
    val meta     = loadMeta(context)
    val filename = getReportFilename(context, "csv", mode)
    val file     = File(getReportDirectory(context), filename)
    val writer   = file.bufferedWriter()

    val reportType = if (mode == ReportMode.BATCH_APPROVAL) "APROBACIÓN DE LOTE" else "INFORME NCR"
    writer.write("TIPO;$reportType\n")
    writer.write("Lote;${meta.albaran}\n")
    writer.write("Operario;${meta.operator}\n")
    writer.write("Turno;${meta.shift}\n")
    writer.write("Fecha;${meta.date}\n\n")

    when (mode) {
        ReportMode.BATCH_APPROVAL -> {
            val approved = detections.count { it.status == DetectionStatus.OK }
            val rejected = detections.count { it.status == DetectionStatus.NOT_ACCEPTED }
            writer.write("Total;${detections.size}\n")
            writer.write("Aprobadas;$approved\n")
            writer.write("Rechazadas;$rejected\n\n")
            writer.write("ID;Tipo;Confianza (%);Status;Medida (mm)\n")
            detections.forEach {
                writer.write(
                    "${it.id};${it.type};${"%.1f".format(it.confidence * 100)};${it.status};${it.measurementMm ?: ""}\n"
                )
            }
        }

        ReportMode.PIECE_REJECTION -> {
            writer.write("ID;Tipo;Confianza (%);Status;Medida (mm);BBox;Diagnóstico\n")
            detections.forEach {
                val bbox   = "${it.boundingBox.left},${it.boundingBox.top},${it.boundingBox.width()},${it.boundingBox.height()}"
                val notas  = it.notes.replace(";", ",").replace("\n", " ")
                writer.write(
                    "${it.id};${it.type};${"%.1f".format(it.confidence * 100)};${it.status};${it.measurementMm ?: ""};$bbox;$notas\n"
                )
            }
        }
    }

    writer.close()
    return file
}

// ─────────────────────────────────────────────────────────────────────────────
// WORD (.docx)
// ─────────────────────────────────────────────────────────────────────────────

fun generateWordFromDetections(
    context: Context,
    detections: List<DetectionItem>,
    isGeneral: Boolean = false
): File {
    val mode     = if (isGeneral) ReportMode.BATCH_APPROVAL else ReportMode.PIECE_REJECTION
    val meta     = loadMeta(context)
    val filename = getReportFilename(context, "docx", mode)
    val file     = File(getReportDirectory(context), filename)
    val doc      = org.apache.poi.xwpf.usermodel.XWPFDocument()

    fun addTitle(text: String, size: Int = 18) {
        val p   = doc.createParagraph()
        val run = p.createRun()
        run.isBold    = true
        run.fontSize  = size
        run.setText(text)
    }

    fun addLine(text: String, italic: Boolean = false) {
        val p   = doc.createParagraph()
        val run = p.createRun()
        run.isItalic = italic
        run.setText(text)
    }

    fun addSeparator() = addLine("─".repeat(60))

    when (mode) {

        ReportMode.BATCH_APPROVAL -> {
            val approved = detections.count { it.status == DetectionStatus.OK }
            val rejected = detections.count { it.status == DetectionStatus.NOT_ACCEPTED }
            val passRate = if (detections.isNotEmpty()) approved * 100 / detections.size else 0

            addTitle("CERTIFICADO DE APROBACIÓN DE LOTE")
            addSeparator()
            addLine("Lote / Albarán : ${meta.albaran}")
            addLine("Operario       : ${meta.operator}")
            addLine("Turno          : ${meta.shift}")
            addLine("Fecha          : ${meta.date}")
            addLine("")
            addTitle("RESUMEN", 14)
            addLine("Total inspecciones : ${detections.size}")
            addLine("✔ Aprobadas        : $approved")
            addLine("✖ Rechazadas       : $rejected")
            addLine("Tasa aprobación    : $passRate %")
            addLine("")
            val verdict = if (rejected == 0) "★ LOTE APROBADO ★" else "⚠ LOTE CON NO CONFORMIDADES"
            addTitle(verdict, 16)
            addLine("")
            addTitle("DETALLE", 12)
            addSeparator()
            detections.forEach {
                val medida = it.measurementMm?.let { m -> "%.2f mm".format(m) } ?: "N/A"
                addLine("ID ${it.id}  |  ${it.type}  |  ${"%.1f%%".format(it.confidence * 100)}  |  ${it.status}  |  $medida")
            }
        }

        ReportMode.PIECE_REJECTION -> {
            addTitle("INFORME DE NO CONFORMIDAD (NCR)")
            addSeparator()
            addLine("Lote / Albarán : ${meta.albaran}")
            addLine("Operario       : ${meta.operator}")
            addLine("Turno          : ${meta.shift}")
            addLine("Fecha          : ${meta.date}")
            addLine("")

            detections.forEach { item ->
                val medida = item.measurementMm?.let { "%.2f mm".format(it) } ?: "N/A"
                val bbox   = item.boundingBox
                addTitle("DETECCIÓN #${item.id}", 13)
                addLine("Tipo de defecto  : ${item.type}")
                addLine("Estado           : ${item.status}")
                addLine("Confianza IA     : ${"%.1f%%".format(item.confidence * 100)}")
                addLine("Medida calibrada : $medida")
                addLine("Área afectada    : L:%.0f T:%.0f R:%.0f B:%.0f (%.0fx%.0f px)".format(
                    bbox.left, bbox.top, bbox.right, bbox.bottom, bbox.width(), bbox.height()))
                addLine("")
                if (item.notes.isNotBlank()) {
                    addLine("▶ DIAGNÓSTICO / BLUEPRINT DE RECHAZO :")
                    item.notes.split("|").map { it.trim() }.filter { it.isNotBlank() }
                        .forEach { line -> addLine("   • $line", italic = true) }
                }
                addLine("")
                addLine("Firma operario : ________________________")
                addLine("Firma calidad  : ________________________")
                addSeparator()
            }
        }
    }

    FileOutputStream(file).use { doc.write(it) }
    return file
}

// ─────────────────────────────────────────────────────────────────────────────
// EXCEL (.xlsx)
// ─────────────────────────────────────────────────────────────────────────────

fun generateExcelFromDetections(
    context: Context,
    detections: List<DetectionItem>,
    isGeneral: Boolean = false
): File {
    val mode     = if (isGeneral) ReportMode.BATCH_APPROVAL else ReportMode.PIECE_REJECTION
    val meta     = loadMeta(context)
    val filename = getReportFilename(context, "xlsx", mode)
    val file     = File(getReportDirectory(context), filename)
    val workbook = XSSFWorkbook()

    val headerStyle = workbook.createCellStyle().apply {
        fillForegroundColor = if (mode == ReportMode.BATCH_APPROVAL)
            IndexedColors.LIGHT_GREEN.index else IndexedColors.ROSE.index
        fillPattern = FillPatternType.SOLID_FOREGROUND
        setFont(workbook.createFont().also { it.bold = true })
    }

    val sheet = workbook.createSheet(
        if (mode == ReportMode.BATCH_APPROVAL) "Aprobación Lote" else "No Conformidad NCR"
    )

    // Row 0: metadata
    sheet.createRow(0).createCell(0).setCellValue(
        "${if (mode == ReportMode.BATCH_APPROVAL) "APROBACIÓN" else "NCR"}  |  " +
        "Lote: ${meta.albaran}  |  ${meta.operator}  |  ${meta.shift}  |  ${meta.date}"
    )

    when (mode) {

        ReportMode.BATCH_APPROVAL -> {
            val approved = detections.count { it.status == DetectionStatus.OK }
            val rejected = detections.count { it.status == DetectionStatus.NOT_ACCEPTED }
            sheet.createRow(1).createCell(0).setCellValue(
                "Total: ${detections.size}  |  Aprobadas: $approved  |  Rechazadas: $rejected"
            )
            val headers = listOf("ID", "Tipo", "Confianza (%)", "Status", "Medida (mm)")
            val hRow = sheet.createRow(3)
            headers.forEachIndexed { i, h ->
                hRow.createCell(i).also { it.setCellValue(h); it.cellStyle = headerStyle }
            }
            detections.forEachIndexed { i, item ->
                sheet.createRow(i + 4).apply {
                    createCell(0).setCellValue(item.id.toDouble())
                    createCell(1).setCellValue(item.type.toString())
                    createCell(2).setCellValue(item.confidence * 100.0)
                    createCell(3).setCellValue(item.status.toString())
                    createCell(4).setCellValue(item.measurementMm?.toDouble() ?: 0.0)
                }
            }
            for (i in headers.indices) sheet.autoSizeColumn(i)
        }

        ReportMode.PIECE_REJECTION -> {
            val headers = listOf("ID", "Tipo", "Confianza (%)", "Status", "Medida (mm)",
                                 "BBox X", "BBox Y", "BBox W", "BBox H", "Diagnóstico / Blueprint")
            val hRow = sheet.createRow(2)
            headers.forEachIndexed { i, h ->
                hRow.createCell(i).also { it.setCellValue(h); it.cellStyle = headerStyle }
            }
            detections.forEachIndexed { i, item ->
                val bbox = item.boundingBox
                sheet.createRow(i + 3).apply {
                    createCell(0).setCellValue(item.id.toDouble())
                    createCell(1).setCellValue(item.type.toString())
                    createCell(2).setCellValue(item.confidence * 100.0)
                    createCell(3).setCellValue(item.status.toString())
                    createCell(4).setCellValue(item.measurementMm?.toDouble() ?: 0.0)
                    createCell(5).setCellValue(bbox.left.toDouble())
                    createCell(6).setCellValue(bbox.top.toDouble())
                    createCell(7).setCellValue(bbox.width().toDouble())
                    createCell(8).setCellValue(bbox.height().toDouble())
                    createCell(9).setCellValue(item.notes)
                }
            }
            for (i in headers.indices) sheet.autoSizeColumn(i)
        }
    }

    FileOutputStream(file).use { workbook.write(it) }
    workbook.close()
    return file
}

// ─────────────────────────────────────────────────────────────────────────────
// JSON
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Exports a structured JSON report.
 * - Approval mode: wraps detections in a summary envelope with pass/fail counts.
 * - Rejection mode: exports full DetectionItem objects including notes/blueprint.
 */
fun exportJsonSummary(
    context: Context,
    detections: List<DetectionItem>,
    isGeneral: Boolean = false
): File {
    val mode     = if (isGeneral) ReportMode.BATCH_APPROVAL else ReportMode.PIECE_REJECTION
    val meta     = loadMeta(context)
    val filename = getReportFilename(context, "json", mode)
    val file     = File(getReportDirectory(context), filename)
    val gson     = GsonBuilder().setPrettyPrinting().create()

    val payload = when (mode) {
        ReportMode.BATCH_APPROVAL -> mapOf(
            "reportType"    to "BATCH_APPROVAL",
            "batch"         to meta.albaran,
            "operator"      to meta.operator,
            "shift"         to meta.shift,
            "date"          to meta.date,
            "totalCount"    to detections.size,
            "approvedCount" to detections.count { it.status == DetectionStatus.OK },
            "rejectedCount" to detections.count { it.status == DetectionStatus.NOT_ACCEPTED },
            "detections"    to detections
        )
        ReportMode.PIECE_REJECTION -> mapOf(
            "reportType" to "PIECE_REJECTION_NCR",
            "batch"      to meta.albaran,
            "operator"   to meta.operator,
            "shift"      to meta.shift,
            "date"       to meta.date,
            "detections" to detections
        )
    }

    FileWriter(file).use { it.write(gson.toJson(payload)) }
    return file
}
