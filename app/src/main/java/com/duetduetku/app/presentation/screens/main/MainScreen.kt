package com.duetduetku.app.presentation.screens.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.duetduetku.app.navigation.Routes
import com.duetduetku.app.presentation.components.BottomNavBar
import com.duetduetku.app.presentation.screens.home.HomeScreen
import com.duetduetku.app.presentation.screens.profile.ProfileScreen
import com.duetduetku.app.presentation.screens.stats.StatsScreen
import kotlinx.coroutines.launch

/**
 * Main Screen wrapper that handles Swipe Navigation + Bottom Bar
 */
@Composable
fun MainScreen(
    navController: NavHostController,
    onNavigateToAlerts: () -> Unit,
    onNavigateToManualInput: () -> Unit,
    onNavigateToScanReceipt: () -> Unit,
    onNavigateToVoiceInput: () -> Unit,
    onNavigateToAllTransactions: () -> Unit,
    onNavigateToEdit: (Long) -> Unit
) {
    val pages = listOf(
        Routes.Home,
        Routes.Stats,
        Routes.Profile
    )
    
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { pages.size }
    )
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNavBar(
                // Use the current page to determine the "route" for the bottom bar selection
                currentRoute = pages[pagerState.currentPage].route,
                onNavigate = { route ->
                    // Find which page corresponds to the clicked route
                    val pageIndex = pages.indexOfFirst { it.route == route }
                    if (pageIndex >= 0) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pageIndex)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            // Load adjacent pages for smoother swipe
            beyondViewportPageCount = 1
        ) { page ->
            when (pages[page]) {
                Routes.Home -> HomeScreen(
                    onNavigateToAlerts = { 
                        // "Alerts" icon is now Profile Icon. Clicking it goes to Profile Tab (index 2)
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(2)
                        }
                    },
                    onNavigateToManualInput = onNavigateToManualInput,
                    onNavigateToScanReceipt = onNavigateToScanReceipt,
                    onNavigateToVoiceInput = onNavigateToVoiceInput,
                    onNavigateToAllTransactions = onNavigateToAllTransactions,
                    onNavigateToEdit = onNavigateToEdit
                )
                Routes.Stats -> StatsScreen(
                    // Back logic stays separate if we want Stats internal nav
                    // For now, back just minimizes app or does nothing as it is top level
                    onNavigateBack = { /* No-op for top level or maybe scroll to home? */ }
                )
                Routes.Profile -> ProfileScreen(
                    onNavigateToBack = { /* No-op for top level */ }
                )
                else -> { /* Other routes are not displayed in pager */ }
            }
        }
    }
}
