package com.skanderjabouzi.colorpicker

import android.content.res.Configuration
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.skanderjabouzi.colorpicker.ui.theme.ColorPickerTheme

/**
 * Demo that shows picking a color from a color wheel, which then dynamically updates
 * the color of a [TopAppBar]. This pattern could also be used to update the value of a
 * Colors, updating the overall theme for an application.
 */

@Composable
fun ColorPicker(
    colorPickerWidth: Dp,
    colorPickerHeight: Dp,
    magnifierWidth: Dp,
    magnifierHeight: Dp,
    selectionCircleDiameter: Dp,
    onColorChange: (Color) -> Unit
) {
    BoxWithConstraints {
        val dimensions = Pair(colorPickerWidth.value, colorPickerHeight.value)
        var position by remember { mutableStateOf(Offset.Zero) }
        val colorPicker =
            remember(dimensions) { ColorPickerCanvas(dimensions.first, dimensions.second) }

        val inputModifier = Modifier.pointerInput(colorPicker) {
            fun updateColorPicker(newPosition: Offset) {
                // Work out if the new position is inside the circle we are drawing, and has a
                // valid color associated to it. If not, keep the current position
                val newColor = colorPicker.colorForPosition(newPosition)
                if (newColor.isSpecified) {
                    position = newPosition
                    onColorChange(newColor)
                }
            }

            awaitEachGesture {
                val down = awaitFirstDown()
                updateColorPicker(down.position)
                drag(down.id) { change ->
                    change.consume()
                    updateColorPicker(change.position)
                }
            }
        }

        Box(
            Modifier
                .clip(RoundedCornerShape(16.dp))
                .clip(shape = RectangleShape)
        ) {
            Image(
                modifier = inputModifier,
                contentDescription = null,
                bitmap = colorPicker.image
            )
            val color = colorPicker.colorForPosition(position)
            Magnifier(
                magnifierWidth = magnifierWidth,
                magnifierHeight = magnifierHeight,
                selectionCircleDiameter = selectionCircleDiameter,
                position = position,
                color = color
            )
        }
    }
}

/**
 * Magnifier displayed on top of [position] with the currently selected [color].
 */
@Composable
private fun Magnifier(
    magnifierWidth: Dp,
    magnifierHeight: Dp,
    selectionCircleDiameter: Dp,
    position: Offset,
    color: Color
) {
    val offset = with(LocalDensity.current) {
        Modifier.offset(
            position.x.toDp() - magnifierWidth / 2,
            // Align with the center of the selection circle
            position.y.toDp() - magnifierHeight / 2
        )
    }
    MagnifierTransition(
        magnifierWidth = magnifierWidth,
        selectionCircleDiameter = selectionCircleDiameter
    ) { _, _, alpha: Float ->
        Column(
            offset
                .size(width = magnifierWidth, height = magnifierHeight)
                .alpha(alpha)
        ) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CustomMagnifier(width = magnifierWidth, height = magnifierHeight, color = color)
            }
        }
    }
}


/**
 * [Transition] that animates between [visible] states of the magnifier by animating the width of
 * the label, diameter of the selection circle, and alpha of the overall magnifier
 */
@Composable
private fun MagnifierTransition(
    magnifierWidth: Dp,
    selectionCircleDiameter: Dp,
    content: @Composable (labelWidth: Dp, selectionDiameter: Dp, alpha: Float) -> Unit
) {
    val transition = updateTransition(true, label = "")
    val labelWidth by transition.animateDp(transitionSpec = { tween() }, label = "") {
        if (it) magnifierWidth else 0.dp
    }
    val magnifierDiameter by transition.animateDp(transitionSpec = { tween() }, label = "") {
        if (it) selectionCircleDiameter else 0.dp
    }
    val alpha by transition.animateFloat(
        transitionSpec = {
            if (true isTransitioningTo false) {
                tween(delayMillis = 100, durationMillis = 200)
            } else {
                tween()
            }
        }, label = ""
    ) {
        if (it) 1f else 0f
    }
    content(labelWidth, magnifierDiameter, alpha)
}

/**
 * A color wheel with an [ImageBitmap] that draws a circular color wheel of the specified diameter.
 */
private class ColorPickerCanvas(width: Float, height: Float) {

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

/**
 * @return the matching color for [position] inside [ColorPickerCanvas], or `null` if there is no color
 * or the color is partially transparent.
 */
private fun ColorPickerCanvas.colorForPosition(position: Offset): Color {
    val x = position.x.toInt().coerceAtLeast(0)
    val y = position.y.toInt().coerceAtLeast(0)
    with(image.toPixelMap()) {
        if (x >= width || y >= height) return Color.Unspecified
        return this[x, y].takeIf { it.alpha == 1f } ?: Color.Unspecified
    }
}

@Composable
private fun CustomMagnifier(
    width: Dp,
    height: Dp,
    color: Color,
) {
    Surface(
        modifier = Modifier
            .height(height)
            .width(width)
            .padding(10.dp)
            .clip(
                RoundedCornerShape(
                    topStart = 8.dp,
                    topEnd = 8.dp,
                    bottomEnd = 8.dp,
                    bottomStart = 80.dp,
                )
            ),
        color = Color.White
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
    CustomMagnifier(60.dp, 100.dp, Color.Cyan)
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun ColorPickerDemoPreview() {
    data class ButtonsColors(var brightness: Color, val color: Color, val temperature: Color)

    val buttonsColors = ButtonsColors(Color.Transparent, Color.Transparent, Color.Transparent)
    var surfaceColor by remember { mutableStateOf(Color(0xFF6200EE)) }
    var colorPickerVisible by remember { mutableStateOf(true) }
    var buttonsColorsState by remember { mutableStateOf(buttonsColors) }
    ColorPickerTheme {
        Column {
            ColorPicker(
                colorPickerWidth = 300.dp,
                colorPickerHeight = 500.dp,
                magnifierWidth = 60.dp,
                magnifierHeight = 100.dp,
                selectionCircleDiameter = 30.dp,
                onColorChange = { surfaceColor = it }
            )
            Row(
                modifier = Modifier
                    .padding(5.dp)
                    .background(Color.Gray),
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = {
                        colorPickerVisible = false
                        buttonsColorsState = buttonsColors.copy(brightness = Color.Red)
                    },
                    modifier = Modifier
                        .size(30.dp)
                        .border(1.dp, buttonsColorsState.brightness, shape = CircleShape)
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (isSystemInDarkTheme()) R.drawable.brightness_dark else R.drawable.brightness_light
                        ),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(5.dp))

                IconButton(
                    onClick = {
                        colorPickerVisible = true
                        buttonsColorsState = buttonsColors.copy(color = Color.Red)
                    },
                    modifier = Modifier
                        .size(30.dp)
                        .border(1.dp, buttonsColorsState.color, shape = CircleShape)
                ) {
                    Icon(
                        painter = painterResource(
                            id = R.drawable.color_gradient
                        ),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        tint = Color.Unspecified
                    )
                }

                Spacer(modifier = Modifier.width(5.dp))

                Button(
                    onClick = {
                        colorPickerVisible = false
                        buttonsColorsState = buttonsColors.copy(temperature = Color.Red)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = surfaceColor),
                    modifier = Modifier
                        .size(30.dp)
                        .border(1.dp, buttonsColorsState.temperature, shape = CircleShape)
                ) {
                }
            }
        }
    }
}