package com.duetduetku.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.duetduetku.app.presentation.screens.input.ManualInputScreen
import com.duetduetku.app.presentation.screens.main.MainScreen
import com.duetduetku.app.presentation.screens.scan.ScanReceiptScreen
import com.duetduetku.app.presentation.screens.transactions.AllTransactionsTodayScreen
import com.duetduetku.app.presentation.screens.voice.VoiceInputScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    // No global Scaffold/BottomBar here. It's moved to MainScreen.
    
    NavHost(
        navController = navController,
        // Start at Home, which will be our MainScreen entry point
        startDestination = Routes.Home.route,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                tween(300)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                tween(300)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                tween(300)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                tween(300)
            )
        }
    ) {
        // Main Screen (Contains Home, Stats, Profile with Swipe)
        composable(Routes.Home.route) {
            MainScreen(
                navController = navController,
                onNavigateToAlerts = { navController.navigate(Routes.Alerts.route) },
                onNavigateToManualInput = { navController.navigate(Routes.ManualInput.route) },
                onNavigateToScanReceipt = { navController.navigate(Routes.ScanReceipt.route) },
                onNavigateToVoiceInput = { navController.navigate(Routes.VoiceInput.route) },

                onNavigateToAllTransactions = { navController.navigate(Routes.AllTransactionsToday.route) },
                onNavigateToEdit = { transactionId ->
                    navController.navigate("${Routes.ManualInput.route}?transactionId=$transactionId")
                }
            )
        }
        
        // We removed individual Stats and Profile composables because they are now inside MainScreen
        // If we need deep links to them, we would need to pass arguments to MainScreen to set the initial page
        
        // Full Screen Screens
        composable(
            route = "${Routes.ManualInput.route}?amount={amount}&merchant={merchant}&date={date}&transactionId={transactionId}",
            arguments = listOf(
                androidx.navigation.navArgument("amount") { 
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                androidx.navigation.navArgument("merchant") { 
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                androidx.navigation.navArgument("date") { 
                    type = androidx.navigation.NavType.LongType
                    defaultValue = 0L
                },
                androidx.navigation.navArgument("transactionId") { 
                    type = androidx.navigation.NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val amountStr = backStackEntry.arguments?.getString("amount")
            val merchant = backStackEntry.arguments?.getString("merchant")
            val date = backStackEntry.arguments?.getLong("date")?.takeIf { it != 0L }
            val transactionId = backStackEntry.arguments?.getLong("transactionId") ?: 0L
            
            ManualInputScreen(
                onNavigateBack = { navController.popBackStack() },
                initialAmount = amountStr?.toDoubleOrNull(),
                initialMerchant = merchant,
                initialDate = date,
                transactionId = transactionId
            )
        }
        
        composable(Routes.ScanReceipt.route) {
            ScanReceiptScreen(
                onNavigateBack = { navController.popBackStack() },
                onScanSuccess = { amount, date, merchant ->
                    val amountParam = if (amount != null) "amount=$amount" else ""
                    val encodedMerchant = if (merchant != null) java.net.URLEncoder.encode(merchant, "UTF-8") else ""
                    val merchantParam = if (encodedMerchant.isNotEmpty()) "merchant=$encodedMerchant" else ""
                    val dateParam = if (date != null) "date=$date" else ""
                    
                    val params = listOf(amountParam, merchantParam, dateParam)
                        .filter { it.isNotEmpty() }
                        .joinToString("&")
                    
                    val route = if (params.isNotEmpty()) "${Routes.ManualInput.route}?$params" else Routes.ManualInput.route
                    
                    navController.navigate(route)
                }
            )
        }
        
        composable(Routes.VoiceInput.route) {
            VoiceInputScreen(
                onNavigateBack = { navController.popBackStack() },
                onAnalysisSuccess = { amount, merchant, date ->
                    val amountParam = if (amount != null) "amount=$amount" else ""
                    val encodedMerchant = if (merchant != null) android.net.Uri.encode(merchant) else ""
                    val merchantParam = if (encodedMerchant.isNotEmpty()) "merchant=$encodedMerchant" else ""
                    val dateParam = if (date != null) "date=$date" else ""
                    
                    val params = listOf(amountParam, merchantParam, dateParam)
                        .filter { it.isNotEmpty() }
                        .joinToString("&")
                    
                    val route = if (params.isNotEmpty()) "${Routes.ManualInput.route}?$params" else Routes.ManualInput.route
                    
                    // Pop VoiceInput so we don't return to it on Back/Save
                    navController.popBackStack()
                    navController.navigate(route)
                }
            )
        }
        

        
        composable(Routes.AllTransactionsToday.route) {
            AllTransactionsTodayScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { transactionId ->
                    navController.navigate("${Routes.ManualInput.route}?transactionId=$transactionId")
                }
            )
        }
    }
}
