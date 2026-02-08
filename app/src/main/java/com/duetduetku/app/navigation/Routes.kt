package com.duetduetku.app.navigation

/**
 * Sealed class defining all navigation routes in the app
 */
sealed class Routes(val route: String) {
    // Bottom Navigation Screens
    data object Home : Routes("home")
    data object Stats : Routes("stats")
    data object Profile : Routes("profile")
    
    // Full Screen (No Bottom Nav)
    data object ManualInput : Routes("manual-input")
    data object ScanReceipt : Routes("scan-receipt")
    data object VoiceInput : Routes("voice-input")
    data object Alerts : Routes("alerts")
    data object AllTransactionsToday : Routes("all-transactions-today")
}

/**
 * List of routes where bottom navigation should be visible
 */
val bottomNavRoutes = listOf(
    Routes.Home.route,
    Routes.Stats.route,
    Routes.Profile.route
)
