package com.duetduetku.app.presentation.screens.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.duetduetku.app.presentation.viewmodel.CategoryStat
import com.duetduetku.app.presentation.viewmodel.StatsState
import com.duetduetku.app.presentation.viewmodel.StatsViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Preview(showBackground = true)
@Composable
fun StatsScreenPreview() {
    val dummyState = StatsState(
        selectedPeriod = "This Month",
        totalIncome = 5000000.0,
        totalExpense = 3916000.0,
        categoryBreakdown = listOf(
            CategoryStat("Education", 3500000.0, 0.89f, "#2E7D32"),
            CategoryStat("Health", 300000.0, 0.07f, "#C62828"),
            CategoryStat("Food", 50000.0, 0.01f, "#F57C00"),
            CategoryStat("Transport", 9000.0, 0.002f, "#1976D2")
        ),
        dailyStats = emptyList()
    )
    
    StatsContent(
        uiState = dummyState,
        onNavigateBack = {},
        onPeriodChange = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    StatsContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onPeriodChange = viewModel::onPeriodChange,
        onDateSelected = viewModel::onDateSelected
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsContent(
    uiState: StatsState,
    onNavigateBack: () -> Unit,
    onPeriodChange: (String) -> Unit,
    onDateSelected: (java.util.Date) -> Unit = {}
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var showYearPicker by remember { mutableStateOf(false) }

    // Helper for formatting displayed date range
    val dateRangeLabel = remember(uiState.selectedPeriod, uiState.selectedDate) {
        val date = uiState.selectedDate
        val calendar = java.util.Calendar.getInstance()
        calendar.time = date
        when (uiState.selectedPeriod) {
            "This Week" -> {
                // Formatting "dd MMM - dd MMM yyyy"
                val startCal = calendar.clone() as java.util.Calendar
                // Adjust to week start
                val day = startCal.get(java.util.Calendar.DAY_OF_WEEK)
                val diff = if (day == java.util.Calendar.SUNDAY) 6 else day - java.util.Calendar.MONDAY
                startCal.add(java.util.Calendar.DAY_OF_MONTH, -diff)
                
                val endCal = startCal.clone() as java.util.Calendar
                endCal.add(java.util.Calendar.DAY_OF_MONTH, 6)
                
                val fmt = SimpleDateFormat("dd MMM", Locale.getDefault())
                val fmtYear = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                "${fmt.format(startCal.time)} - ${fmtYear.format(endCal.time)}"
            }
            "Month" -> SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(date)
            "Years" -> SimpleDateFormat("yyyy", Locale.getDefault()).format(date)
            else -> ""
        }
    }

    if (showDatePicker) {
        val datePickerState = androidx.compose.material3.rememberDatePickerState(
             initialSelectedDateMillis = uiState.selectedDate.time
        )
        androidx.compose.material3.DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        onDateSelected(java.util.Date(it))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            androidx.compose.material3.DatePicker(state = datePickerState)
        }
    }

    if (showMonthPicker) {
        MonthPickerDialog(
            currentDate = uiState.selectedDate,
            onMonthSelected = {
                 onDateSelected(it)
                 showMonthPicker = false
            },
            onDismiss = { showMonthPicker = false }
        )
    }

    if (showYearPicker) {
        YearPickerDialog(
            currentDate = uiState.selectedDate,
            onYearSelected = {
                onDateSelected(it)
                showYearPicker = false
            },
            onDismiss = { showYearPicker = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 2.dp), // Top spacing and Nav bar spacing
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Period Selector
            item {
                PeriodSelector(
                    selectedPeriod = uiState.selectedPeriod,
                    onPeriodSelected = { period ->
                         onPeriodChange(period)
                    }
                )
            }
            
            // Summary
            item {
                SummarySection(uiState)
            }

            // Chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = when (uiState.selectedPeriod) {
                                        "Month" -> "Expense Breakdown"
                                        "Years" -> "Annual Expenses"
                                        else -> "Daily Expense"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                // Optional: Show current date selection as subtitle if needed
                                Text(
                                    text = dateRangeLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            androidx.compose.material3.IconButton(
                                onClick = {
                                    when (uiState.selectedPeriod) {
                                        "This Week" -> showDatePicker = true
                                        "Month" -> showMonthPicker = true
                                        "Years" -> showYearPicker = true
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = "Filter Date",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        if (uiState.selectedPeriod == "Month") {
                            DonutChart(
                                categories = uiState.categoryBreakdown,
                                totalExpense = uiState.totalExpense,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp)
                            )
                        } else {
                            SimpleBarChart(
                                stats = uiState.dailyStats,
                                labels = uiState.dailyStats.map { 
                                    val pattern = if (uiState.selectedPeriod == "Years") "MMM" else "dd"
                                    SimpleDateFormat(pattern, Locale.getDefault()).format(it.date) 
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                        }
                    }
                }
            }

            // Category Breakdown
            item {
                Text(
                    text = "Spending by Category",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (uiState.categoryBreakdown.isEmpty()) {
                item {
                    Text(
                        text = "No expenses in this period",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            } else {
                items(uiState.categoryBreakdown) { category ->
                    CategoryRow(category)
                }
            }
        }
    }
}

@Composable
fun PeriodSelector(selectedPeriod: String, onPeriodSelected: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        listOf("This Week", "Month", "Years").forEach { period ->
            val isSelected = selectedPeriod == period
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .clickable { onPeriodSelected(period) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = period,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun SummarySection(state: StatsState) {
    val formatter = com.duetduetku.app.util.CurrencyFormatter

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFE8F5E9)) // Light Green
                .padding(16.dp)
        ) {
            Text("Income", style = MaterialTheme.typography.labelMedium, color = Color(0xFF2E7D32))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatter.format(state.totalIncome).replace(formatter.getSymbol(), "").trim(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFFFEBEE)) // Light Red
                .padding(16.dp)
        ) {
            Text("Expense", style = MaterialTheme.typography.labelMedium, color = Color(0xFFC62828))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatter.format(state.totalExpense).replace(formatter.getSymbol(), "").trim(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC62828)
            )
        }
    }
}

@Composable
fun SimpleBarChart(
    stats: List<com.duetduetku.app.presentation.viewmodel.DailyStat>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    if (stats.isEmpty()) return

    val maxVal = stats.maxOfOrNull { it.amount.toFloat() } ?: 1f
    
    val isSystemDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val labelColor = if (isSystemDark) android.graphics.Color.LTGRAY else android.graphics.Color.DKGRAY
    val primaryColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val barWidth = size.width / (stats.size * 2f + 1f) // Gap + Bar + Gap
        val maxBarContentHeight = size.height - 60f // Leave space for labels (bottom) and dots (top)

        stats.forEachIndexed { index, stat ->
            val value = stat.amount.toFloat()
            // Calculate Height
            val ratio = if (maxVal > 0) value / maxVal else 0f
            var barHeight = ratio * maxBarContentHeight
            // Ensure min height for visual pill shape even if 0
            if (barHeight < barWidth) barHeight = barWidth 
            
            val left = barWidth * (index * 2 + 1)
            val bottom = size.height - 40f // Leave space for text
            val top = bottom - barHeight
            
            // 1. Define Capsule Path (Container)
            val barPath = Path().apply {
                addRoundRect(
                    RoundRect(
                        left = left,
                        top = top,
                        right = left + barWidth,
                        bottom = bottom,
                        cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                    )
                )
            }

            // 2. Draw Stacked Segments inside the Capsule
            clipPath(barPath) {
                // Default background if no segments (e.g. 0 amount but min height)
                drawRect(
                    color = Color.Gray.copy(alpha = 0.3f),
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight)
                )

                var currentY = bottom
                val segments = stat.segments
                val totalSegAmount = segments.sumOf { it.amount }.toFloat()
                
                segments.forEach { segment ->
                    val segHeight = if (totalSegAmount > 0) {
                        (segment.amount.toFloat() / totalSegAmount) * barHeight
                    } else 0f
                    
                    val segColor = try {
                        Color(android.graphics.Color.parseColor(segment.colorHex))
                    } catch (e: Exception) {
                        primaryColor
                    }

                    drawRect(
                        color = segColor,
                        topLeft = Offset(left, currentY - segHeight),
                        size = Size(barWidth, segHeight)
                    )
                    currentY -= segHeight
                }
            }

            // 3. Draw Hatched Texture (Stripes) over the whole bar
            clipPath(barPath) {
                val stripeColor = Color.Black.copy(alpha = 0.1f)
                val stripeWidth = 2.dp.toPx()
                val stripeGap = 6.dp.toPx()
                
                val startX = left - barHeight
                val endX = left + barWidth + barHeight
                
                var x = startX
                while (x < endX) {
                    drawLine(
                        color = stripeColor,
                        start = Offset(x, bottom),
                        end = Offset(x + barHeight, bottom - barHeight), // 45 degree up-right
                        strokeWidth = stripeWidth
                    )
                    x += stripeGap
                }
            }
            
            // 4. Floating Dot
            // Use color of the largest segment (dominant) for the dot
            val dotColorHex = stat.segments.firstOrNull()?.colorHex ?: "#CCCCCC"
            val dotColor = try {
                Color(android.graphics.Color.parseColor(dotColorHex))
            } catch (e: Exception) {
                Color.Gray
            }
            
            val dotY = top - 12.dp.toPx()
            drawCircle(
                color = dotColor,
                radius = 3.dp.toPx(),
                center = Offset(left + barWidth / 2, dotY)
            )

            // 5. Labels
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    labels.getOrElse(index) { "" },
                    left + barWidth / 2,
                    size.height - 10f,
                    android.graphics.Paint().apply {
                        color = labelColor
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = 30f
                        isAntiAlias = true
                        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                    }
                )
            }
        }
    }
}

@Composable
fun SimpleLineChart(
    data: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val maxVal = data.maxOrNull() ?: 1f
    val activeColor = Color(0xFFC6FF00) // Neon Lime
    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val lineColor = if (isSystemDark) activeColor else MaterialTheme.colorScheme.primary
    val labelColor = if (isSystemDark) android.graphics.Color.LTGRAY else android.graphics.Color.DKGRAY

    Canvas(modifier = modifier) {
        val spacing = size.width / (data.size - 1).coerceAtLeast(1)
        val maxChartHeight = size.height - 60f
        
        val path = Path()
        val points = mutableListOf<Offset>()

        data.forEachIndexed { index, value ->
            val ratio = if (maxVal > 0) value / maxVal else 0f
            val x = index * spacing
            val y = (size.height - 40f) - (ratio * maxChartHeight)
            
            points.add(Offset(x, y))
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        // Draw Line
        drawPath(
            path = path,
            color = lineColor,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
        )
        
        // Draw Dots and Labels
        points.forEachIndexed { index, offset ->
            drawCircle(
                color = lineColor,
                radius = 4.dp.toPx(),
                center = offset
            )
            
            // Draw label every 5th item to avoid clutter in Month view (30 items)
            if (index % 5 == 0 || index == data.size - 1) {
                 drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        labels.getOrElse(index) { "" },
                        offset.x,
                        size.height - 10f,
                        android.graphics.Paint().apply {
                            color = labelColor
                            textAlign = android.graphics.Paint.Align.CENTER
                            textSize = 30f
                            isAntiAlias = true
                        }
                    )
                }
            }
        }
    }
    }


@Composable
fun DonutChart(
    categories: List<CategoryStat>,
    totalExpense: Double,
    modifier: Modifier = Modifier
) {
    if (categories.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No data", color = Color.Gray)
        }
        return
    }

    val totalValue = categories.sumOf { it.amount }
    val proportions = categories.map { it.amount.toFloat() / totalValue.toFloat() }
    val colors = categories.map { 
        try {
            Color(android.graphics.Color.parseColor(it.colorHex))
        } catch (e: Exception) {
            MaterialTheme.colorScheme.primary
        }
    }

    val isSystemDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val titleColor = if (isSystemDark) android.graphics.Color.LTGRAY else android.graphics.Color.GRAY
    val amountColor = if (isSystemDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK

    Canvas(modifier = modifier) {
        val strokeWidth = 50f
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset(
            (size.width - diameter) / 2,
            (size.height - diameter) / 2
        )
        val size = Size(diameter, diameter)
        
        var startAngle = -90f

        categories.forEachIndexed { index, category ->
            val sweepAngle = proportions[index] * 360f
            val color = colors[index]
            
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round),
                topLeft = topLeft,
                size = size
            )
            
            startAngle += sweepAngle
        }

        // Draw Center Text
        drawContext.canvas.nativeCanvas.apply {
            val paintTitle = android.graphics.Paint().apply {
                color = titleColor
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = 40f
                isAntiAlias = true
            }
            val paintAmount = android.graphics.Paint().apply {
                color = amountColor
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = 60f
                isAntiAlias = true
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            }
            
            val centerX = size.width / 2 + topLeft.x
            val centerY = size.height / 2 + topLeft.y
            
            drawText("TOTAL SPENT", centerX, centerY - 20f, paintTitle)
            
            // Format Amount
            val formatted = com.duetduetku.app.util.CurrencyFormatter.format(totalExpense)
            drawText(formatted, centerX, centerY + 50f, paintAmount)
        }
    }
}

@Composable
fun CategoryRow(item: CategoryStat) {
    // Determine icon based on category name
    val icon = when (item.category.lowercase()) {
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

    val categoryColor = try {
        Color(android.graphics.Color.parseColor(item.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    val mainTextColor = MaterialTheme.colorScheme.onSurface
    val subTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    
    // User requested specific #2d3231 for "Spending by Category" card in dark mode
    val cardBgColor = if (isDark) Color(0xFF2D3231) else Color.White
    val shadowColor = if (isDark) Color.White.copy(alpha = 0.25f) else Color.Black
    val textColor = if (isDark) Color.White else Color.Black

    val shadowModifier = Modifier.shadow(
        elevation = 4.dp, 
        shape = RoundedCornerShape(24.dp), 
        spotColor = shadowColor, 
        ambientColor = shadowColor
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .then(shadowModifier)
            .clip(RoundedCornerShape(24.dp))
            .background(cardBgColor) 
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with colored background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(categoryColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = categoryColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Category Name and Percentage
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.category,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = "${(item.percentage * 100).toInt()}% of budget",
                    style = MaterialTheme.typography.bodySmall,
                    color = subTextColor
                )
            }

            // Amount
            Text(
                text = com.duetduetku.app.util.CurrencyFormatter.format(item.amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress Bar
        LinearProgressIndicator(
            progress = { item.percentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = categoryColor,
            trackColor = trackColor, 
        )
    }
}

@Composable
fun MonthPickerDialog(
    currentDate: java.util.Date,
    onMonthSelected: (java.util.Date) -> Unit,
    onDismiss: () -> Unit
) {
    val months = java.text.DateFormatSymbols().shortMonths
    val calendar = java.util.Calendar.getInstance()
    calendar.time = currentDate
    val currentYear = calendar.get(java.util.Calendar.YEAR)

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Month ($currentYear)") },
        text = {
            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(3),
                modifier = Modifier.height(200.dp)
            ) {
                items(months.size) { index ->
                    val isSelected = index == calendar.get(java.util.Calendar.MONTH)
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                            .clickable {
                                calendar.set(java.util.Calendar.MONTH, index)
                                onMonthSelected(calendar.time)
                            }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = months[index],
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun YearPickerDialog(
    currentDate: java.util.Date,
    onYearSelected: (java.util.Date) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = java.util.Calendar.getInstance()
    val currentYear = calendar.get(java.util.Calendar.YEAR)
    val years = (currentYear downTo 2000).toList()
    
    calendar.time = currentDate
    val selectedYear = calendar.get(java.util.Calendar.YEAR)

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Year") },
        text = {
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.height(300.dp)
            ) {
                items(years) { year ->
                    val isSelected = year == selectedYear
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                calendar.set(java.util.Calendar.YEAR, year)
                                onYearSelected(calendar.time)
                            }
                            .padding(12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = year.toString(),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
