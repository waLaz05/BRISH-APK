package com.katchy.focuslive.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SimpleFlowRow(
    modifier: Modifier = Modifier,
    verticalGap: Dp = 0.dp,
    horizontalGap: Dp = 0.dp,
    alignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val hGapPx = horizontalGap.roundToPx()
        val vGapPx = verticalGap.roundToPx()

        val rows = mutableListOf<List<Placeable>>()
        val rowWidths = mutableListOf<Int>()
        val rowHeights = mutableListOf<Int>()

        var currentRow = mutableListOf<Placeable>()
        var currentWidth = 0
        var currentHeight = 0

        measurables.forEach { measurable ->
            val placeable = measurable.measure(constraints)
            if (currentWidth + placeable.width > constraints.maxWidth) {
                rows.add(currentRow)
                rowWidths.add(currentWidth - hGapPx)
                rowHeights.add(currentHeight)
                
                currentRow = mutableListOf(placeable)
                currentWidth = placeable.width + hGapPx
                currentHeight = placeable.height
            } else {
                currentRow.add(placeable)
                currentWidth += placeable.width + hGapPx
                currentHeight = maxOf(currentHeight, placeable.height)
            }
        }
        
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
            rowWidths.add(currentWidth - hGapPx)
            rowHeights.add(currentHeight)
        }

        val height = rowHeights.sum() + (rowHeights.size - 1).coerceAtLeast(0) * vGapPx

        layout(width = constraints.maxWidth, height = height) {
            var yOffset = 0
            rows.forEachIndexed { i, row ->
                val rowWidth = rowWidths[i]
                var xOffset = when(alignment) {
                    Alignment.End -> constraints.maxWidth - rowWidth
                    Alignment.CenterHorizontally -> (constraints.maxWidth - rowWidth) / 2
                    else -> 0
                }
                
                row.forEach { placeable ->
                    placeable.place(x = xOffset, y = yOffset)
                    xOffset += placeable.width + hGapPx
                }
                yOffset += rowHeights[i] + vGapPx
            }
        }
    }
}
