package com.example.celestic.viewmodel


sealed class DashboardState {
    object Idle : DashboardState()                     // Placeholder visible, cámara apagada
    object CameraReady : DashboardState()              // Cámara encendida, esperando captura
    object Processing : DashboardState()               // Analizando frame
    data class Approved(val detectionId: Long) : DashboardState() // Resultado OK → mostrar menú
    data class NavigateToDetails(val detectionId: Long) :
        DashboardState() // Resultado WARNING/NOT_ACCEPTED

    data class Error(val message: String) : DashboardState()
}