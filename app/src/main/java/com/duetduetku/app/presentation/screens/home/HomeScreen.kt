package com.duetduetku.app.presentation.screens.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.duetduetku.app.presentation.components.BudgetCard
import com.duetduetku.app.presentation.components.ExpandableFab
import com.duetduetku.app.presentation.components.TransactionCard
import com.duetduetku.app.presentation.viewmodel.HomeViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.border
import com.duetduetku.app.presentation.components.EditLimitDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAlerts: () -> Unit,
    onNavigateToManualInput: () -> Unit,
    onNavigateToScanReceipt: () -> Unit,
    onNavigateToVoiceInput: () -> Unit,
    onNavigateToAllTransactions: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isFabExpanded by remember { mutableStateOf(false) }
    var showLimitDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showLimitDialog) {
        EditLimitDialog(
            currentLimit = uiState.dailyLimit.toInt().toString(),
            onDismiss = { showLimitDialog = false },
            onConfirm = {
                val limit = it.toDoubleOrNull() ?: 0.0
                viewModel.updateDailyLimit(limit)
                showLimitDialog = false
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp) // Extra padding for FAB and scrolling
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hello, ${uiState.userName}!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Welcome back",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                    
                    // Profile Photo Icon
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8D5B7))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            .clickable { onNavigateToAlerts() }, // Assuming this navigates to Profile view
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.profilePhotoUri != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(uiState.profilePhotoUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Profile Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = Color(0xFF8B7355),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // Budget Card
            item {
                BudgetCard(
                    limit = uiState.dailyLimit,
                    spent = uiState.todayExpense,
                    onBudgetClick = { showLimitDialog = true },
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            // Recent Activity Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "See All",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onNavigateToAllTransactions() }
                    )
                }
            }

            // Transaction List
            if (uiState.recentTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No transactions today yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                items(uiState.recentTransactions.take(5)) { transaction ->
                    TransactionCard(
                        transaction = transaction,
                        modifier = Modifier
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                            .clickable { onNavigateToEdit(transaction.id) }
                    )
                }
            }
        }

        // Floating Action Button
        ExpandableFab(
            expanded = isFabExpanded,
            onExpandChange = { isFabExpanded = it },
            onManualClick = { 
                isFabExpanded = false
                onNavigateToManualInput() 
            },
            onScanClick = { 
                isFabExpanded = false
                onNavigateToScanReceipt() 
            },
            onVoiceClick = { 
                isFabExpanded = false
                onNavigateToVoiceInput() 
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 24.dp)
        )
    }
}
