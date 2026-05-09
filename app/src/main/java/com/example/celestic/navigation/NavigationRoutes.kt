package com.example.celestic.navigation

/**
 * Defines all navigation routes used in the application.
 *
 * Each route is represented as a sealed class object to ensure type‑safety
 * and avoid hard‑coded strings throughout the codebase.
 */
sealed class NavigationRoutes(val route: String) {

    /** Main dashboard screen. */
    object Dashboard : NavigationRoutes("dashboard")

    /**
     * Details screen route.
     *
     * Supports:
     * - detailType (required)
     * - id (optional)
     *
     * Example:
     *   details/hole?id=42
     */
    object Details : NavigationRoutes("details/{detailType}?id={id}") {
        fun createRoute(detailType: String, id: Long? = null): String {
            return "details/$detailType" + (if (id != null) "?id=$id" else "")
        }
    }

    /** Calibration screen. */
    object Calibration : NavigationRoutes("calibration")

    /** Report request dialog. */
    object ReportDialog : NavigationRoutes("report_dialog")

    /** Reports list screen. */
    object Reports : NavigationRoutes("reports")

    /** Status screen. */
    object Status : NavigationRoutes("status")
}
