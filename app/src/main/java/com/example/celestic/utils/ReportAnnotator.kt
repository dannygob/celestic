package com.example.celestic.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import com.example.celestic.models.DetectionItem
import com.example.celestic.models.enums.DetectionStatus
import java.io.File
import java.io.FileOutputStream

/**
 * Draws defect blueprint annotations (bounding boxes + labels) on top of the
 * saved frame image for a rejected DetectionItem, and writes the result to a
 * temporary file that can then be embedded into PDF/Word/Excel reports.
 *
 * Color coding:
 *   RED    → NOT_ACCEPTED  (hard rejection)
 *   ORANGE → WARNING       (tolerance exceeded)
 *   GREEN  → OK            (pass — rarely shown in NCR reports)
 */
object ReportAnnotator {

    /**
     * Produces an annotated bitmap for [items] drawn over their shared frame.
     * The frame is read from [context].filesDir/detection_images/[frameId].jpg.
     *
     * Returns null if the source frame image does not exist on disk
     * (e.g. it was already deleted because the piece was approved).
     */
    fun annotateFrame(
        context: Context,
        frameId: String,
        items: List<DetectionItem>
    ): Bitmap? {
        val imageFile = File(context.filesDir, "detection_images/$frameId.jpg")
        if (!imageFile.exists()) return null

        val source = BitmapFactory.decodeFile(imageFile.absolutePath) ?: return null

        // Work on a mutable copy so we do not alter the original
        val annotated = source.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(annotated)

        val scaleX = annotated.width.toFloat()
        val scaleY = annotated.height.toFloat()

        items.forEach { item ->
            val bbox   = item.boundingBox
            val color  = when (item.status) {
                DetectionStatus.NOT_ACCEPTED -> Color.RED
                DetectionStatus.WARNING      -> Color.parseColor("#FF8C00")  // dark orange
                DetectionStatus.OK           -> Color.GREEN
            }

            // Bounding box stroke
            val boxPaint = Paint().apply {
                style       = Paint.Style.STROKE
                this.color  = color
                strokeWidth = (annotated.width * 0.004f).coerceAtLeast(3f)
                isAntiAlias = true
            }

            // Semi-transparent fill
            val fillPaint = Paint().apply {
                style      = Paint.Style.FILL
                this.color = Color.argb(40,
                    Color.red(color), Color.green(color), Color.blue(color))
            }

            val left   = bbox.left   * scaleX / scaleX  // coordinates are already in px
            val top    = bbox.top    * scaleY / scaleY
            val right  = bbox.right  * scaleX / scaleX
            val bottom = bbox.bottom * scaleY / scaleY

            canvas.drawRect(left, top, right, bottom, fillPaint)
            canvas.drawRect(left, top, right, bottom, boxPaint)

            // Label background
            val labelText  = "#${item.id} ${item.type}"
            val fontSize   = (annotated.width * 0.025f).coerceIn(18f, 48f)
            val textPaint  = Paint().apply {
                this.color  = Color.WHITE
                textSize    = fontSize
                typeface    = Typeface.DEFAULT_BOLD
                isAntiAlias = true
            }
            val labelBgPaint = Paint().apply {
                this.color = color
                style      = Paint.Style.FILL
            }

            val textBounds = Rect()
            textPaint.getTextBounds(labelText, 0, labelText.length, textBounds)
            val labelPadding = fontSize * 0.25f
            val labelTop     = (top - textBounds.height() - labelPadding * 2).coerceAtLeast(0f)
            val labelBottom  = labelTop + textBounds.height() + labelPadding * 2
            val labelRight   = left + textBounds.width() + labelPadding * 2

            canvas.drawRect(left, labelTop, labelRight, labelBottom, labelBgPaint)
            canvas.drawText(
                labelText,
                left + labelPadding,
                labelBottom - labelPadding,
                textPaint
            )

            // Measurement tag (bottom-right corner of bbox)
            item.measurementMm?.let { mm ->
                val measureText = "%.2f mm".format(mm)
                val measurePaint = Paint().apply {
                    this.color  = Color.WHITE
                    textSize    = fontSize * 0.85f
                    typeface    = Typeface.DEFAULT_BOLD
                    isAntiAlias = true
                }
                val measureBgPaint = Paint().apply {
                    this.color = Color.argb(180, 0, 0, 0)
                    style      = Paint.Style.FILL
                }
                val mBounds = Rect()
                measurePaint.getTextBounds(measureText, 0, measureText.length, mBounds)
                val mPad    = fontSize * 0.2f
                val mLeft   = (right - mBounds.width() - mPad * 2).coerceAtLeast(0f)
                val mBottom = bottom.coerceAtMost(annotated.height.toFloat())
                val mTop    = (mBottom - mBounds.height() - mPad * 2).coerceAtLeast(0f)
                canvas.drawRect(mLeft, mTop, right, mBottom, measureBgPaint)
                canvas.drawText(measureText, mLeft + mPad, mBottom - mPad, measurePaint)
            }
        }

        return annotated
    }

    /**
     * Saves an annotated bitmap to a temporary file inside the app's cache
     * directory so it can be embedded in the report and later discarded.
     *
     * @return The created JPEG file, or null if the source frame is unavailable.
     */
    fun saveAnnotatedFrame(
        context: Context,
        frameId: String,
        items: List<DetectionItem>,
        quality: Int = 88
    ): File? {
        val bitmap = annotateFrame(context, frameId, items) ?: return null
        val out    = File(context.cacheDir, "annotated_${frameId}.jpg")
        FileOutputStream(out).use { bitmap.compress(Bitmap.CompressFormat.JPEG, quality, it) }
        bitmap.recycle()
        return out
    }
}
