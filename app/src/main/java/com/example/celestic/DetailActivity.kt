package com.example.celestic

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.celestic.R

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
                title = "Hole Detection"
                labelTextView.text = "Details for Hole Detection"
                // measurementTextView.text = "Measurement: ..."
                // nominalTextView.text = "Nominal: ..."
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
