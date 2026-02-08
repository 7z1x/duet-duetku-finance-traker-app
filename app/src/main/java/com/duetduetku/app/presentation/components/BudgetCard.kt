package com.duetduetku.app.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duetduetku.app.ui.theme.Primary
import com.duetduetku.app.ui.theme.Accent
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BudgetCard(
    limit: Double,
    spent: Double,
    onBudgetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val remaining = limit - spent
    val progress = (spent / limit).coerceIn(0.0, 1.0).toFloat()
    
    // Format currency
    val formatter = com.duetduetku.app.util.CurrencyFormatter
    val remainingFormatted = formatter.format(remaining).replace(formatter.getSymbol(), "").trim()
    val limitFormatted = formatter.format(limit)

    val primaryColor = MaterialTheme.colorScheme.primary
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .softShadow(elevation = 15.dp, shape = MaterialTheme.shapes.medium, spotColor = primaryColor.copy(alpha = 0.3f))
            .clip(MaterialTheme.shapes.medium)
            .background(primaryColor)
            .clickable(onClick = onBudgetClick)
            .padding(24.dp)
    ) {
        // Decorative background blobs similar to CSS
        Box(
            modifier = Modifier
                .offset(x = 20.dp, y = (-20).dp)
                .align(Alignment.TopEnd)
                .size(160.dp)
                .background(Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(100))
        )
        Box(
            modifier = Modifier
                .offset(x = (-20).dp, y = 20.dp)
                .align(Alignment.BottomStart)
                .size(100.dp)
                .background(Accent.copy(alpha = 0.2f), shape = RoundedCornerShape(100))
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Today's Budget",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Text(
                        text = "Daily Limit: $limitFormatted",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Remaining: $remainingFormatted",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.3f))
                    .border(2.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(50))
            ) {
                val animatedProgress by animateFloatAsState(targetValue = progress, label = "Progress")
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(Color.White)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${(progress * 100).toInt()}% SPENT",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.9f),
                    letterSpacing = 1.sp
                )
                Text(
                    text = "$remainingFormatted LEFT",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.9f),
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
