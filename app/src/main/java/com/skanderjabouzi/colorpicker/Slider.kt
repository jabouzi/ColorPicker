package com.skanderjabouzi.colorpicker

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Slider(
    modifier: Modifier = Modifier,
    height: Dp = 240.dp,
    width: Dp = 80.dp,
    range: IntRange = 0..100,
    gradientColorBottom: Color = Color.Black,
    gradientColorTop: Color = Color.White
) {
    val localDensity = LocalDensity.current
    var gradientHeight by remember { mutableStateOf(height / 2) }
    val sliderValue = range.last - ((gradientHeight / height) * range.last)

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .rotate(180f)
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { delta ->
                    val deltaDp = with(localDensity) { delta.toDp() }
                    gradientHeight =
                        (gradientHeight + deltaDp).coerceIn(0.dp, height)
                }
            )
    ) {
        val size = with(localDensity) { height.toPx() }
        Box(
            modifier = Modifier
                .width(width)
                .height(height)
                .background(Color(0x214f4a4a))
        )
        Box(
            modifier = Modifier
                .width(width)
                .height(gradientHeight)
                .background(
                    Brush.verticalGradient(
                        // note: the whole slider component is rotated 180 deg. so the arguments
                        // specified in this gradient are reversed from what you'd expect
                        colors = listOf(gradientColorBottom, gradientColorTop),
                        endY = size,
                        startY = 0f
                    )
                )
        )
    }
}

@Composable
@Preview
fun SliderPreview() {
    Slider()
}

@Composable
@Preview(showBackground = true)
fun ColorfulSliderPreview() {
    Slider(
        gradientColorBottom = Color.White,
        gradientColorTop = Color(0xFF9C27B0),
        range = 0..25
    )
}

@Composable
@Preview
fun ShortSliderPreview() {
    Slider(
        height = 200.dp
    )
}
