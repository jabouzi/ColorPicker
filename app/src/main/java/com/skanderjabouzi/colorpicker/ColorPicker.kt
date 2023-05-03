package com.skanderjabouzi.colorpicker

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SweepGradientShader
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Component for picking a color from a color wheel, which then dynamically updates
 * the color of a any other component.
 */

@Composable
fun ColorPicker(
    colorPickerWidth: Dp,
    colorPickerHeight: Dp,
    magnifierWidth: Dp,
    magnifierHeight: Dp,
    onColorChange: (Color) -> Unit,
    onPositionChange: (Offset) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints {
        val dimensions = Pair(colorPickerWidth.value, colorPickerHeight.value)
        var position by remember { mutableStateOf(Offset.Zero) }
        var color by remember { mutableStateOf(Color(0xFF83eb34)) }
        val colorPicker =
            remember(dimensions) { ColorPickerCanvas(dimensions.first, dimensions.second) }

        Box(
            Modifier
                .clip(RoundedCornerShape(16.dp))
                .clip(shape = RectangleShape)
        ) {
            Image(
                modifier = modifier,
                contentDescription = null,
                bitmap = colorPicker.image
            )

            ColorSelector(
                magnifierWidth = magnifierWidth,
                magnifierHeight = magnifierHeight,
                position = position,
                color = color,
                onPositionChange = {
                    position = it
                    color = colorPicker.colorForPosition(position)
                    onColorChange(color)
                    onPositionChange(position)
                }

            )
        }
    }
}

@Composable
private fun ColorSelector(
    magnifierWidth: Dp,
    magnifierHeight: Dp,
    position: Offset,
    color: Color,
    onPositionChange: (Offset) -> Unit
) {
    var offsetState by remember { mutableStateOf(position) }
    CustomPointer(
        width = magnifierWidth,
        height = magnifierHeight,
        color = color,
        onDragListener = {
            offsetState = it
            onPositionChange(offsetState)
        }
    )
}

private class ColorPickerCanvas(
    width: Float,
    height: Float,
) {
    private val sweepGradient = SweepGradientShader(
        colors = listOf(
            Color.Red,
            Color.Magenta,
            Color.Blue,
            Color.Cyan,
            Color.Green,
            Color.Yellow,
            Color.Red
        ),
        colorStops = null,
        center = Offset(width / 2, height / 2)
    )

    val image = ImageBitmap(width.toInt(), height.toInt()).also { imageBitmap ->
        val canvas = Canvas(imageBitmap)
        val paint = Paint().apply { shader = sweepGradient }
        canvas.drawRect(0F, 0F, width, height, paint)
    }
}

private fun ColorPickerCanvas.colorForPosition(
    position: Offset,
): Color {
    val x = position.x.toInt().coerceAtLeast(0)
    val y = position.y.toInt().coerceAtLeast(0)
    with(image.toPixelMap()) {
        if (x >= width || y >= height) return Color.Unspecified
        return this[x, y].takeIf { it.alpha == 1f } ?: Color.Unspecified
    }
}

@Composable
private fun CustomPointer(
    width: Dp,
    height: Dp,
    color: Color,
    onDragListener: (Offset) -> Unit
) {
    var offsetStateX by remember { mutableStateOf(0f) }
    var offsetStateY by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetStateX.roundToInt(), offsetStateY.roundToInt()) }
            .height(height)
            .width(width)
            .padding(10.dp)
            .clip(
                shape =
                RoundedCornerShape(
                    topStart = 8.dp,
                    topEnd = 8.dp,
                    bottomEnd = 8.dp,
                    bottomStart = 80.dp,
                )
            )
            .background(Color.White)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetStateX += dragAmount.x
                    offsetStateY += dragAmount.y
                    onDragListener(
                        Offset(
                            offsetStateX + width.value / 2 + dragAmount.x,
                            offsetStateY + height.value + dragAmount.y
                        )
                    )
                    println("offsetStateX: $offsetStateX, offsetStateY: $offsetStateY")
                    println("change.position.x: ${change.position.x}, change.position.y: ${change.position.y}")
                }
            }
    ) {
        Column(
            Modifier.padding(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(color, RoundedCornerShape(8))
                    .height(height / 3)
                    .width(width / 2)
            )
            {

            }
        }
    }
}

@Preview
@Composable
private fun MagnifierTest() {
    CustomPointer(60.dp, 100.dp, Color.Cyan, {})
}

//@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
//@Composable
//fun ColorPickerDemoPreview() {
//    data class ButtonsColors(var brightness: Color, val color: Color, val temperature: Color)
//
//    val buttonsColors = ButtonsColors(Color.Transparent, Color.Transparent, Color.Transparent)
//    var surfaceColor by remember { mutableStateOf(Color(0xFF83eb34)) }
//    var colorPickerVisible by remember { mutableStateOf(true) }
//    var buttonsColorsState by remember { mutableStateOf(buttonsColors) }
//    ColorPickerTheme {
//        Column {
//            ColorPicker(
//                colorPickerWidth = 300.dp,
//                colorPickerHeight = 500.dp,
//                magnifierWidth = 60.dp,
//                magnifierHeight = 100.dp,
//                onColorChange = { surfaceColor = it },
//                onPositionChange = {}
//            )
//            Row(
//                modifier = Modifier
//                    .padding(5.dp)
//                    .background(Color.Gray),
//                horizontalArrangement = Arrangement.Center
//            ) {
//                IconButton(
//                    onClick = {
//                        colorPickerVisible = false
//                        buttonsColorsState = buttonsColors.copy(brightness = Color.Red)
//                    },
//                    modifier = Modifier
//                        .size(30.dp)
//                        .border(1.dp, buttonsColorsState.brightness, shape = CircleShape)
//                ) {
//                    Icon(
//                        painter = painterResource(
//                            id = if (isSystemInDarkTheme()) R.drawable.brightness_dark else R.drawable.brightness_light
//                        ),
//                        contentDescription = null,
//                        modifier = Modifier.fillMaxSize()
//                    )
//                }
//
//                Spacer(modifier = Modifier.width(5.dp))
//
//                IconButton(
//                    onClick = {
//                        colorPickerVisible = true
//                        buttonsColorsState = buttonsColors.copy(color = Color.Red)
//                    },
//                    modifier = Modifier
//                        .size(30.dp)
//                        .border(1.dp, buttonsColorsState.color, shape = CircleShape)
//                ) {
//                    Icon(
//                        painter = painterResource(
//                            id = R.drawable.color_gradient
//                        ),
//                        contentDescription = null,
//                        modifier = Modifier.fillMaxSize(),
//                        tint = Color.Unspecified
//                    )
//                }
//
//                Spacer(modifier = Modifier.width(5.dp))
//
//                Button(
//                    onClick = {
//                        colorPickerVisible = false
//                        buttonsColorsState = buttonsColors.copy(temperature = Color.Red)
//                    },
//                    colors = ButtonDefaults.buttonColors(containerColor = surfaceColor),
//                    modifier = Modifier
//                        .size(30.dp)
//                        .border(1.dp, buttonsColorsState.temperature, shape = CircleShape)
//                ) {
//                }
//            }
//        }
//    }
//}