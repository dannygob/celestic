package com.example.celestic.manager

import android.content.Context
import com.example.celestic.models.TraceabilityItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TraceabilityManager loads and manages traceability metadata for parts/components.
 *
 * The data is stored in a JSON file inside the assets folder and contains
 * information such as part codes, descriptions, and traceability rules.
 *
 * This manager provides a simple lookup function to retrieve traceability
 * information based on a part code.
 */
@Singleton
class TraceabilityManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Lazily loaded list of traceability items.
     * The JSON file is parsed only once when first accessed.
     */
    private val traceabilityItems: List<TraceabilityItem> by lazy {
        loadTraceabilityData()
    }

    /**
     * Loads traceability data from the assets folder.
     *
     * @return A list of TraceabilityItem objects parsed from JSON.
     *         Returns an empty list if the file cannot be read or parsed.
     */
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

    /**
     * Looks up a traceability item by its part code.
     *
     * @param code The part or component code to search for.
     * @return The matching TraceabilityItem, or null if not found.
     */
    fun lookup(code: String): TraceabilityItem? {
        return traceabilityItems.find { it.code == code }
    }
}
