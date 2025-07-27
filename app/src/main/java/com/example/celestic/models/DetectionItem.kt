package com.example.celestic.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DetectionItem(
    val id: String,
    val type: String,
    val description: String,
    val imageUrl: String
) : Parcelable
