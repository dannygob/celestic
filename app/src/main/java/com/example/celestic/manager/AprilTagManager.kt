package com.example.celestic.manager

// Requiere configuración de CMake, librería nativa compilada y llamada desde Kotlin con mat.nativeObjAddr.

object AprilTagManager {
    external fun detectTags(nativeMat: Long): Array<String>
}