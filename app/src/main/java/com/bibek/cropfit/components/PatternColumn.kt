package com.bibek.cropfit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PatternedColumn(modifier: Modifier, content: @Composable ColumnScope.() -> Unit) {
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFEDF7ED).copy(alpha = 0.95f), // very light green
            Color(0xFFD7EAD9).copy(alpha = 0.95f)  // light earthy green
        )
    )

    val overlayStripes = Brush.linearGradient(
        colors = listOf(
            Color(0xFFB5CDA3).copy(alpha = 0.08f), Color.Transparent
        ), start = Offset.Zero, end = Offset.Infinite
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .drawWithContent {
                drawContent()
                drawRect(overlayStripes)
            }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), content = content
        )
    }
}

