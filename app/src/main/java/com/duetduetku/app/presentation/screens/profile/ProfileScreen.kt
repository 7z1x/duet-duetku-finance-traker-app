package com.duetduetku.app.presentation.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.duetduetku.app.presentation.components.EditLimitDialog
import com.duetduetku.app.presentation.components.EditNameDialog
import com.duetduetku.app.presentation.components.ThemeSelectionDialog
import com.duetduetku.app.presentation.viewmodel.ProfileViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showLimitDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { 
            // Grant permission for future access
            try {
                context.contentResolver.takePersistableUriPermission(it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) {
                // Ignore if not possible
            }
            viewModel.updateProfilePhoto(it.toString())
        }
    }

    if (showEditNameDialog) {
        EditNameDialog(
            currentName = uiState.userName,
            onDismiss = { showEditNameDialog = false },
            onConfirm = { 
                viewModel.updateName(it)
                showEditNameDialog = false
            }
        )
    }

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
    
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = uiState.appTheme,
            onDismiss = { showThemeDialog = false },
            onThemeSelected = { 
                viewModel.setAppTheme(it)
                showThemeDialog = false
            }
        )
    }



    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = 40.dp)
    ) {
        // Profile Photo Section
        item {
            ProfilePhotoSection(
                photoUri = uiState.profilePhotoUri,
                onPhotoClick = { pickMedia.launch("image/*") }
            )
        }
        
        // Name Section
        item {
            Spacer(modifier = Modifier.height(24.dp))
            NameSection(
                name = if (uiState.userName.isNotEmpty()) uiState.userName else "User",
                onEditClick = { showEditNameDialog = true }
            )
        }
        
        // Settings Section
        item {
            Spacer(modifier = Modifier.height(40.dp))
            ProfileSettingCard(
                icon = Icons.Default.AttachMoney,
                iconBackgroundColor = Color(0xFF3ECC2E),
                iconContainerColor = Color(0xFF3ECC2E),
                iconTint = Color.White,
                title = "Daily Budget Limit",
                value = "Rp ${java.text.NumberFormat.getNumberInstance(java.util.Locale("id", "ID")).format(uiState.dailyLimit.toInt())}",
                onClick = { showLimitDialog = true }
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(12.dp))
            ProfileSettingCard(
                icon = Icons.Default.Palette,
                iconBackgroundColor = Color(0xFF805AD5),
                title = "App Theme",
                value = when(uiState.appTheme) { 1 -> "Light"; 2 -> "Dark"; else -> "System" },
                onClick = { showThemeDialog = true }
            )
        }
        

        
        // Danger Zone
        item {
            Spacer(modifier = Modifier.height(32.dp))
            ProfileSettingCard(
                icon = Icons.Default.DeleteForever,
                iconBackgroundColor = Color(0xFFE53E3E),
                title = "Reset App",
                value = null,
                onClick = viewModel::clearAllData,
                isDanger = true
            )
        }
        
        if (uiState.isLoading) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
fun ProfilePhotoSection(
    photoUri: String?,
    onPhotoClick: () -> Unit
) {
    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        // Profile Photo Circle
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(3.dp, MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                .background(Color(0xFFE8D5B7))
                .clickable(onClick = onPhotoClick),
            contentAlignment = Alignment.Center
        ) {
            if (photoUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photoUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Photo",
                    tint = Color(0xFF8B7355),
                    modifier = Modifier.size(60.dp)
                )
            }
        }
        
        // Camera Button Overlay
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 4.dp, y = 4.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(onClick = onPhotoClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Change Photo",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun NameSection(name: String, onEditClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        IconButton(
            onClick = onEditClick,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit Name",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun ProfileSettingCard(
    icon: ImageVector,
    iconBackgroundColor: Color,
    title: String,
    value: String?,
    onClick: () -> Unit,
    isDanger: Boolean = false,
    iconTint: Color? = null,
    iconContainerColor: Color? = null
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val shadowColor = if (isDark) Color.White.copy(alpha = 0.25f) else Color.Black
    val cardBg = if (isDark) Color(0xFF2D3231) else Color.White
    val textColor = if (isDark) Color.White else Color.Black

    val finalIconTint = iconTint ?: iconBackgroundColor
    val finalIconContainerColor = iconContainerColor ?: iconBackgroundColor.copy(alpha = 0.2f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp, 
                shape = RoundedCornerShape(16.dp), 
                spotColor = shadowColor, 
                ambientColor = shadowColor
            )
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon with colored background
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(finalIconContainerColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = finalIconTint,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = if (isDanger) iconBackgroundColor else textColor,
            modifier = Modifier.weight(1f)
        )
        
        // Value or Arrow
        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}
