package com.example.celestic.manager

import android.content.Context
import com.example.celestic.models.TraceabilityItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraceabilityManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val traceabilityItems: List<TraceabilityItem> by lazy {
        loadTraceabilityData()
    }

    private fun loadTraceabilityData(): List<TraceabilityItem> {
        return try {
            val inputStream = context.assets.open("config/traceability.json")
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<List<TraceabilityItem>>() {}.type
            Gson().fromJson(reader, type)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun lookup(code: String): TraceabilityItem? {
        return traceabilityItems.find { it.code == code }
    }
}
