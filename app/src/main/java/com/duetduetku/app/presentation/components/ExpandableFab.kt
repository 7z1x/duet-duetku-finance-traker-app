package com.duetduetku.app.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.duetduetku.app.ui.theme.*

@Composable
fun ExpandableFab(
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onManualClick: () -> Unit,
    onScanClick: () -> Unit,
    onVoiceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FabOption(
                    icon = Icons.Default.EditNote,
                    label = "Manual Input",
                    color = Lavender,
                    iconTint = Color(0xFF5B4DBC), // Indigo-ish
                    onClick = onManualClick
                )
                FabOption(
                    icon = Icons.Default.CameraAlt,
                    label = "Scan Receipt",
                    color = Secondary,
                    iconTint = Color(0xFF0F5145), // Teal-ish
                    onClick = onScanClick
                )
                FabOption(
                    icon = Icons.Default.Mic,
                    label = "Voice Note",
                    color = Accent,
                    iconTint = Color(0xFF944B00), // Orange-ish
                    onClick = onVoiceClick
                )
            }
        }

        Box(
            modifier = Modifier
                .size(64.dp)
                .fabShadow(shape = RoundedCornerShape(50))
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.primary)
                .border(4.dp, MaterialTheme.colorScheme.surface, RoundedCornerShape(50))
                .clickable { onExpandChange(!expanded) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (expanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = "Expand FAB",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(32.dp)
                    .rotate(if (expanded) 0f else 0f)
            )
        }
    }
}

@Composable
private fun FabOption(
    icon: ImageVector,
    label: String,
    color: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .softShadow(shape = MaterialTheme.shapes.small)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Box(
            modifier = Modifier
                .size(48.dp)
                .softShadow(shape = RoundedCornerShape(50))
                .clip(RoundedCornerShape(50))
                .background(color)
                .border(2.dp, Color.White, RoundedCornerShape(50)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint
            )
        }
    }
}
