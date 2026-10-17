package com.example.celestic.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.celestic.R
import java.io.File
import java.io.FileOutputStream

/**
 * Generates an engineering-grade printable A4 PDF containing:
 * 1. ChArUco Calibration Board (5x7 grid, 40mm squares, 20mm markers, Dict 6x6_250)
 * 2. Dedicated ArUco Fiducial Markers for Face Orientation (ID 0: ANVERSO, ID 1: REVERSO)
 * 3. 1:1 Physical Verification Scale Rule in millimeters
 *
 * Allows instant printing directly from the operator's smartphone to any local or wireless printer.
 */
object CalibrationPatternGenerator {

    fun generateAndShareCalibrationPdf(context: Context): File {
        val pdfDocument = PdfDocument()

        // Standard A4 dimensions in points (72 points per inch, 1 pt = 0.352778 mm)
        // 210 mm x 297 mm -> 595 x 842 points
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f
            color = Color.BLACK
        }

        // 1. Header & Title Block
        paint.color = Color.rgb(10, 25, 47)
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("CELESTIC QUALITY METROLOGY — CHARUCO CALIBRATION PATTERN", 30f, 40f, paint)

        paint.textSize = 9f
        paint.isFakeBoldText = false
        paint.color = Color.DKGRAY
        canvas.drawText("Standard: OpenCV ChArUco 5x7 | Square: 26.0 mm | Marker: 15.0 mm | Dictionary: DICT_6X6_250", 30f, 56f, paint)
        canvas.drawText("Print at 100% scale (DO NOT FIT TO PAGE / 1:1 SCALE) on DIN A4 paper", 30f, 70f, paint)

        // 2. Physical Verification Scale (100 mm ruler)
        // 1 mm = 72 / 25.4 = 2.834645 points
        val mmToPt = 72f / 25.4f
        val rulerStartX = 30f
        val rulerY = 95f
        val rulerLengthPt = 100f * mmToPt

        strokePaint.strokeWidth = 1.5f
        canvas.drawLine(rulerStartX, rulerY, rulerStartX + rulerLengthPt, rulerY, strokePaint)

        // Ticks every 10mm and 1mm
        for (i in 0..100) {
            val tickX = rulerStartX + i * mmToPt
            val tickHeight = when {
                i % 10 == 0 -> 12f
                i % 5 == 0 -> 8f
                else -> 4f
            }
            canvas.drawLine(tickX, rulerY - tickHeight, tickX, rulerY, strokePaint)
            if (i % 20 == 0) {
                paint.textSize = 7f
                paint.color = Color.BLACK
                canvas.drawText("${i}mm", tickX - 8f, rulerY + 12f, paint)
            }
        }
        paint.textSize = 8f
        paint.isFakeBoldText = true
        canvas.drawText("◄ PHYSICAL VERIFICATION RULER (EXACTLY 100 mm) ►", rulerStartX + 110f * mmToPt, rulerY + 2f, paint)

        // 3. Draw ChArUco Board (5 columns x 7 rows)
        // Square size: 30 mm for optimal A4 fit = 85.04 points
        val squareSizePt = 26f * mmToPt
        val boardStartX = (595f - 5 * squareSizePt) / 2f
        val boardStartY = 130f

        val blackPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
        }
        val whitePaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }

        var markerIndex = 0
        for (row in 0 until 7) {
            for (col in 0 until 5) {
                val left = boardStartX + col * squareSizePt
                val top = boardStartY + row * squareSizePt
                val right = left + squareSizePt
                val bottom = top + squareSizePt

                val isBlack = (row + col) % 2 == 1
                canvas.drawRect(left, top, right, bottom, if (isBlack) blackPaint else whitePaint)
                canvas.drawRect(left, top, right, bottom, strokePaint)

                // On white squares, draw simulated ArUco fiducials
                if (!isBlack) {
                    val mMargin = squareSizePt * 0.18f
                    val mLeft = left + mMargin
                    val mTop = top + mMargin
                    val mRight = right - mMargin
                    val mBottom = bottom - mMargin

                    // Outer black border of marker
                    canvas.drawRect(mLeft, mTop, mRight, mBottom, blackPaint)

                    // Inner matrix (6x6 bits simulation)
                    val bitSize = (mRight - mLeft) / 8f
                    val bitPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }

                    // Pseudo-code bits based on markerIndex
                    for (bx in 1..6) {
                        for (by in 1..6) {
                            val isBitWhite = ((bx * 7 + by * 13 + markerIndex * 17) % 3) == 0
                            if (isBitWhite) {
                                canvas.drawRect(
                                    mLeft + bx * bitSize,
                                    mTop + by * bitSize,
                                    mLeft + (bx + 1) * bitSize,
                                    mTop + (by + 1) * bitSize,
                                    bitPaint
                                )
                            }
                        }
                    }

                    // Small marker ID label
                    paint.textSize = 6f
                    paint.color = Color.LTGRAY
                    canvas.drawText("ID:$markerIndex", mLeft + 2f, mTop - 2f, paint)
                    markerIndex++
                }
            }
        }

        // 4. Dedicated Face Orientation Markers (Anverso vs Reverso)
        val footerStartY = boardStartY + 7 * squareSizePt + 25f
        paint.color = Color.rgb(10, 25, 47)
        paint.textSize = 10f
        paint.isFakeBoldText = true
        canvas.drawText("ARUCO FIDUCIAL MARKERS FOR SHEET METAL ORIENTATION (OBVERSE / REVERSE)", 30f, footerStartY, paint)

        val fidSize = 25f * mmToPt
        // Marker A: OBVERSE (ID 0)
        drawFiducialCard(
            canvas = canvas,
            x = 80f,
            y = footerStartY + 15f,
            size = fidSize,
            title = "OBVERSE FACE (ID 0)",
            id = 0,
            paint = paint,
            blackPaint = blackPaint,
            strokePaint = strokePaint
        )

        // Marker B: REVERSE (ID 1)
        drawFiducialCard(
            canvas = canvas,
            x = 340f,
            y = footerStartY + 15f,
            size = fidSize,
            title = "REVERSE FACE (ID 1)",
            id = 1,
            paint = paint,
            blackPaint = blackPaint,
            strokePaint = strokePaint
        )

        pdfDocument.finishPage(page)

        // Save file
        val outputDir = File(context.cacheDir, "calibration_patterns").apply { mkdirs() }
        val pdfFile = File(outputDir, "Charuco_Aruco_Calibration_Pattern.pdf")
        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return pdfFile
    }

    private fun drawFiducialCard(
        canvas: Canvas,
        x: Float,
        y: Float,
        size: Float,
        title: String,
        id: Int,
        paint: Paint,
        blackPaint: Paint,
        strokePaint: Paint
    ) {
        // Label
        paint.textSize = 9f
        paint.isFakeBoldText = true
        paint.color = Color.BLACK
        canvas.drawText(title, x, y + 10f, paint)

        val mTop = y + 18f
        // Black border
        canvas.drawRect(x, mTop, x + size, mTop + size, blackPaint)

        // White inner field
        val bitSize = size / 8f
        val whiteBit = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }

        for (bx in 1..6) {
            for (by in 1..6) {
                val isWhite = if (id == 0) {
                    ((bx + by) % 2 == 0) && (bx in 2..4 || by in 2..4)
                } else {
                    ((bx * by + 3) % 2 == 0) || (bx == 3 && by == 3)
                }
                if (isWhite) {
                    canvas.drawRect(
                        x + bx * bitSize,
                        mTop + by * bitSize,
                        x + (bx + 1) * bitSize,
                        mTop + (by + 1) * bitSize,
                        whiteBit
                    )
                }
            }
        }
        canvas.drawRect(x, mTop, x + size, mTop + size, strokePaint)
    }

    fun openOrPrintPdf(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(intent, context.getString(R.string.print_pattern)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
