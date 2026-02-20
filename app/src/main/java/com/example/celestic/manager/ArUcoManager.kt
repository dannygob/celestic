package com.example.celestic.manager

import org.opencv.objdetect.Objdetect
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArUcoManager @Inject constructor() : BaseMarkerManager(
    Objdetect.getPredefinedDictionary(Objdetect.DICT_6X6_250)
)

