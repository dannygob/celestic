package com.example.celestic.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

object LocationUtils {

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(context: Context, onLocationReceived: (Location?) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        try {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).addOnSuccessListener { location: Location? ->
                onLocationReceived(location)
            }.addOnFailureListener {
                onLocationReceived(null)
            }
        } catch (e: Exception) {
            onLocationReceived(null)
        }
    }
}
