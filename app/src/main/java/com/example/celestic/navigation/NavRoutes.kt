package com.example.celestic.navigation

object NavRoutes {
    const val CAMERA = "camera"
    const val DETAILS_HOLE = "detailsHole/{index}"
    const val DETAILS_ALODINE = "detailsAlodine/{index}"
    const val DETAILS_COUNTERSINK = "detailsCountersink/{index}"
    const val STATUS = "status"

    fun detailsHole(index: Int) = "detailsHole/$index"
    fun detailsAlodine(index: Int) = "detailsAlodine/$index"
    fun detailsCountersink(index: Int) = "detailsCountersink/$index"
}