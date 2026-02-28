package com.example.celestic.utils

import android.content.Context
import com.example.celestic.models.TraceabilityItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Utility to load traceability data from JSON in assets.
 */
object JsonLoader {
    fun loadTraceabilityFromJson(context: Context): List<TraceabilityItem> {
        return try {
            val json =
                context.assets.open("config/traceability.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<TraceabilityItem>>() {}.type
            Gson().fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun findByCode(code: String, list: List<TraceabilityItem>): TraceabilityItem? {
        return list.firstOrNull { it.code == code }
    }
}