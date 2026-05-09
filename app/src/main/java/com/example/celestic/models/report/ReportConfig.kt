package com.example.celestic.models.report

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Represents the configuration settings used when generating an inspection report.
 *
 * This entity is stored in Room and allows the system to:
 * - Customize report titles
 * - Enable or disable metadata sections
 * - Include raw feature data or measurements
 * - Select export/output formats (PDF, CSV, JSON, Word, etc.)
 * - Store the timestamp of when the configuration was created
 */
@Entity(tableName = "report_config")
@Parcelize
data class ReportConfig(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /** Title displayed at the top of the generated report. */
    @ColumnInfo(name = "report_title")
    val reportTitle: String,

    /** Whether to include metadata such as operator, batch, timestamps, etc. */
    @ColumnInfo(name = "include_metadata")
    val includeMetadata: Boolean,

    /** Whether to include raw detected features (debug or traceability data). */
    @ColumnInfo(name = "include_raw_features")
    val includeRawFeatures: Boolean,

    /** Export format selected by the user (e.g., "PDF", "CSV", "JSON"). */
    @ColumnInfo(name = "export_format")
    val exportFormat: String,

    /** Timestamp of when the configuration was created ("YYYY-MM-DD HH:mm"). */
    @ColumnInfo(name = "generation_date")
    val generationDate: String,

    /** Whether to embed images (ROIs, blueprint overlays, etc.) in the report. */
    @ColumnInfo(name = "include_images")
    val includeImages: Boolean,

    /** Output format for the final report (e.g., "PDF", "Word", "JSON"). */
    @ColumnInfo(name = "output_format")
    val outputFormat: String,

    /** Whether to include measurement data (distances, tolerances, metrics). */
    @ColumnInfo(name = "include_measurements")
    val includeMeasurements: Boolean

) : Parcelable
