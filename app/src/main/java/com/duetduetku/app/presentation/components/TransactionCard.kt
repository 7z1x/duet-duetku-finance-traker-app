package com.duetduetku.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.duetduetku.app.data.local.entity.Transaction
import com.duetduetku.app.ui.theme.*
import com.duetduetku.app.util.CurrencyUtil
import com.duetduetku.app.util.DateUtil

@Composable
fun TransactionCard(
    transaction: Transaction,
    modifier: Modifier = Modifier
) {
    val icon = when (transaction.category.lowercase()) {
        "food" -> Icons.Default.Restaurant
        "transport" -> Icons.Default.DirectionsBus
        "shopping" -> Icons.Default.ShoppingBag
        "bills" -> Icons.Default.ReceiptLong
        "education" -> Icons.Default.School
        "health" -> Icons.Default.MonitorHeart
        "fun" -> Icons.Default.SportsEsports
        "salary" -> Icons.Default.AttachMoney
        "gift" -> Icons.Default.CardGiftcard
        "investment" -> Icons.Default.TrendingUp
        else -> Icons.Default.Star
    }
    
    val iconColor = when (transaction.category.lowercase()) {
        "food" -> Color(0xFF5B4DBC)
        "transport" -> Color(0xFF448AFF)
        "shopping" -> Color(0xFFE91E63)
        "bills" -> Color(0xFFFF5722)
        "education" -> Color(0xFF3F51B5)
        "health" -> Color(0xFFE91E63)
        "fun" -> Color(0xFF9C27B0)
        "salary" -> Color(0xFF4CAF50)
        "gift" -> Color(0xFFE91E63)
        "investment" -> Color(0xFF2196F3)
        else -> Color.Gray
    }
    
    val iconBg = when (transaction.category.lowercase()) {
        "food" -> Lavender
        "transport" -> Color(0xFF448AFF).copy(alpha = 0.2f)
        "shopping" -> Color(0xFFFCE4EC)
        "bills" -> Color(0xFFFBE9E7)
        "education" -> Color(0xFFE8EAF6)
        "health" -> Color(0xFFFCE4EC)
        "fun" -> Color(0xFFF3E5F5)
        "salary" -> Color(0xFFE8F5E9)
        "gift" -> Color(0xFFFCE4EC)
        "investment" -> Color(0xFFE3F2FD)
        else -> Color.LightGray.copy(alpha = 0.3f)
    }

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val cardBg = if (isDark) Color(0xFF2D3231) else Color.White
    val shadowColor = if (isDark) Color.White.copy(alpha = 0.25f) else Color.Black
    val textColor = if (isDark) Color.White else Color.Black

    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp, 
                shape = MaterialTheme.shapes.small, 
                spotColor = shadowColor, 
                ambientColor = shadowColor
            )
            .background(cardBg, MaterialTheme.shapes.small)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(MaterialTheme.shapes.small)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = transaction.category,
                tint = iconColor
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.note ?: transaction.category,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = "${transaction.category} • ${DateUtil.formatTransactionDate(transaction.date)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
        
        Text(
            text = if (transaction.type.equals("Expense", ignoreCase = true)) "- ${com.duetduetku.app.util.CurrencyFormatter.format(transaction.amount)}" 
                   else "+ ${com.duetduetku.app.util.CurrencyFormatter.format(transaction.amount)}",
            style = MaterialTheme.typography.titleSmall,
            color = if (transaction.type.equals("Expense", ignoreCase = true)) Color(0xFFFF6B6B) else SageGreen,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
