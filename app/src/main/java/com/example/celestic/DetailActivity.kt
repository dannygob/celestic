package com.example.celestic

import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.celestic.models.DetectionItem // Ensure this path is correct
import com.example.celestic.R
import java.io.File
import java.util.ArrayList // For parcelable list, though type may be List

class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val screenType = intent.getStringExtra("SCREEN_TYPE")
        val labelTextView = findViewById<TextView>(R.id.label_textview)
        // You can also find the other TextViews if needed:
        // val measurementTextView = findViewById<TextView>(R.id.measurement_textview)
        // val nominalTextView = findViewById<TextView>(R.id.nominal_textview)

        when (screenType) {
            "HOLE_DETECTION" -> {
                title = "Hole Detection Details" // More specific title
                labelTextView.text = "Detected Holes:" // Update label or remove if redundant

                val imagePath = intent.getStringExtra("PROCESSED_IMAGE_PATH")
                val detectedItems: ArrayList<DetectionItem>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableArrayListExtra("DETECTED_ITEMS", DetectionItem::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableArrayListExtra("DETECTED_ITEMS")
                }

                val imageView = findViewById<ImageView>(R.id.graph_placeholder_imageview)
                val detailsContainer = findViewById<LinearLayout>(R.id.details_container_layout)
                detailsContainer.removeAllViews() // Clear previous details (e.g. placeholder TextViews)

                if (imagePath != null) {
                    val imgFile = File(imagePath)
                    if (imgFile.exists()) {
                        val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                        imageView.setImageBitmap(bitmap)
                        Log.d("DetailActivity", "Image loaded from: $imagePath")
                    } else {
                        Log.e("DetailActivity", "Image file not found: $imagePath")
                        imageView.setImageResource(R.drawable.ic_launcher_background) // Placeholder if file not found
                    }
                } else {
                    Log.e("DetailActivity", "No image path provided.")
                    imageView.setImageResource(R.drawable.ic_launcher_background) // Placeholder if no path
                }

                if (detectedItems != null && detectedItems.isNotEmpty()) {
                    labelTextView.text = "Detected Holes (${detectedItems.size}):"
                    detectedItems.forEachIndexed { index, item ->
                        val itemTextView = TextView(this)
                        val xVal = item.x?.let { String.format("%.2f", it) } ?: "N/A"
                        val yVal = item.y?.let { String.format("%.2f", it) } ?: "N/A"
                        val diameterVal = item.diameter ?: "N/A"

                        itemTextView.text = "Hole ${index + 1}: Pos(${xVal}, ${yVal}), Diameter: ${diameterVal}px"
                        itemTextView.setPadding(16, 8, 16, 8) // Using px, convert to dp for better practice
                        itemTextView.textSize = 16f // sp
                        detailsContainer.addView(itemTextView)
                    }
                } else {
                    labelTextView.text = "Detected Holes:"
                    val noItemsTextView = TextView(this)
                    noItemsTextView.text = "No holes detected."
                    noItemsTextView.setPadding(16, 8, 16, 8)
                    noItemsTextView.textSize = 16f
                    detailsContainer.addView(noItemsTextView)
                }
            }
            "ALODINE_DETECTION" -> {
                title = "Alodine Detection"
                labelTextView.text = "Details for Alodine Detection"
            }
            "COUNTERSINK_DETECTION" -> {
                title = "Countersink Detection"
                labelTextView.text = "Details for Countersink Detection"
            }
            "STATUS" -> {
                title = "Status Information"
                labelTextView.text = "Current System Status"
            }
            else -> {
                title = "Detail Screen"
                labelTextView.text = "No specific type selected or type unknown."
            }
        }
    }
}

// Helper extension function for formatting Doubles
private fun Double.format(digits: Int) = "%.${digits}f".format(this)
