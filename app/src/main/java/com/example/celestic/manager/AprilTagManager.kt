package com.example.celestic.manager

import org.opencv.objdetect.Objdetect
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AprilTagManager @Inject constructor() : BaseMarkerManager(
    Objdetect.getPredefinedDictionary(Objdetect.DICT_APRILTAG_36h11)
)

