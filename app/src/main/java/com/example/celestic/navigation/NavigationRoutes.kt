package com.example.celestic.navigation


sealed class NavigationRoutes(val route: String) {
    object Dashboard : NavigationRoutes("dashboard")
    object Details : NavigationRoutes("details/{detailType}?id={id}") {
        fun createRoute(detailType: String, id: Long? = null) =
            "details/$detailType" + (if (id != null) "?id=$id" else "")
    }

    object Calibration : NavigationRoutes("calibration")
    object ReportDialog : NavigationRoutes("report_dialog")
    object Reports : NavigationRoutes("reports")

    object Status : NavigationRoutes("status")
}
