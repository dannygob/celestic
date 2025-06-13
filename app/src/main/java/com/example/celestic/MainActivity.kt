package com.example.celestic

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
// It's good practice to import R specifically from the app's package
import com.example.celestic.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Use the XML layout

        // Initialize OpenCV if needed (retaining this part from original if relevant)
        // Commenting out OpenCV parts for now as they might interfere or be out of scope
        // if (!OpenCVInitializer.initOpenCV(this)) {
        //     Log.e("OpenCV", "Error initializing OpenCV.")
        //     Toast.makeText(this, "Error initializing OpenCV", Toast.LENGTH_SHORT).show()
        // } else {
        //     Log.d("OpenCV", "OpenCV initialized successfully.")
        // }

        val detectHoleButton = findViewById<Button>(R.id.button_detect_hole)
        detectHoleButton.setOnClickListener {
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("SCREEN_TYPE", "HOLE_DETECTION")
            startActivity(intent)
        }

        val detectAlodineButton = findViewById<Button>(R.id.button_detect_alodine)
        detectAlodineButton.setOnClickListener {
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("SCREEN_TYPE", "ALODINE_DETECTION")
            startActivity(intent)
        }

        val detectCountersinkButton = findViewById<Button>(R.id.button_detect_countersink)
        detectCountersinkButton.setOnClickListener {
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("SCREEN_TYPE", "COUNTERSINK_DETECTION")
            startActivity(intent)
        }

        val statusButton = findViewById<Button>(R.id.button_status)
        statusButton.setOnClickListener {
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("SCREEN_TYPE", "STATUS")
            startActivity(intent)
        }
    }
}
