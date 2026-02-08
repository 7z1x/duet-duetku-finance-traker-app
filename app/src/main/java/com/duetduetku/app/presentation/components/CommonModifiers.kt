package com.duetduetku.app.presentation.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.softShadow(
    elevation: Dp = 10.dp,
    shape: Shape,
    spotColor: Color = Color.Black.copy(alpha = 0.04f),
    ambientColor: Color = Color.Black.copy(alpha = 0.04f)
) = this.shadow(
    elevation = elevation,
    shape = shape,
    spotColor = spotColor,
    ambientColor = ambientColor
)

fun Modifier.fabShadow(
    elevation: Dp = 12.dp,
    shape: Shape,
    spotColor: Color = Color(0xFFFFB9B2).copy(alpha = 0.5f) // Primary color shadow
) = this.shadow(
    elevation = elevation,
    shape = shape,
    spotColor = spotColor,
    ambientColor = spotColor
)
