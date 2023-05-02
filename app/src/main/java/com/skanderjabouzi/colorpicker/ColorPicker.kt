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
import androidx.compose.foundation.shape.GenericShape
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.skanderjabouzi.colorpicker.ui.theme.ColorPickerTheme
import java.util.Locale

/**
 * Demo that shows picking a color from a color wheel, which then dynamically updates
 * the color of a [TopAppBar]. This pattern could also be used to update the value of a
 * Colors, updating the overall theme for an application.
 */

private val MagnifierWidth = 60.dp
private val MagnifierHeight = 100.dp
private val MagnifierLabelHeight = 50.dp
private val SelectionCircleDiameter = 30.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPickerDemo() {
    data class ButtonsColors(var brightness: Color, val color: Color, val temperature: Color)
    val buttonsColors = ButtonsColors(Color.Transparent, Color.Transparent, Color.Transparent)
    var surfaceColor by remember { mutableStateOf(Color(0xFF6200EE)) }
    var colorPickerVisible by remember { mutableStateOf(true) }
    var buttonsColorsState by remember { mutableStateOf(buttonsColors) }
    Surface() {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopAppBar(title = { Text("Color Picker") })
           ColorPicker(
               isVisible = colorPickerVisible,
               onColorChange = { surfaceColor = it }
           )
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(
                    onClick = { colorPickerVisible = false
                        buttonsColorsState = buttonsColors.copy(brightness = Color.Red)
                    },
                    modifier = Modifier
                        .size(50.dp)
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

                Spacer(modifier = Modifier.width(10.dp))

                IconButton(
                    onClick = { colorPickerVisible = true
                        buttonsColorsState = buttonsColors.copy(color = Color.Red)
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .border(1.dp, buttonsColorsState.color, shape = CircleShape)
                ) {
                    Icon(
                        painter = painterResource(
                            id = R.drawable.color_gradient_svg
                        ),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        tint = Color.Unspecified
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                IconButton(
                    onClick = { colorPickerVisible = false
                        buttonsColorsState = buttonsColors.copy(temperature = Color.Red)
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .border(1.dp, buttonsColorsState.temperature, shape = CircleShape)
                        .background(color = surfaceColor, shape = CircleShape)
                ) {
                }
            }
        }
    }
}

@Composable
private fun ColorPicker(
    isVisible: Boolean,
    onColorChange: (Color) -> Unit
) {
    BoxWithConstraints(
        Modifier
            .padding(top = 100.dp, start = 100.dp, end = 100.dp, bottom = 50.dp)
            .size(size = 300.dp)
            .clip(shape = RectangleShape)
            .aspectRatio(0.6F)
            .clip(RoundedCornerShape(16.dp))
    ) {
        if (isVisible) {
            val dimensions = Pair(constraints.maxWidth, constraints.maxHeight)
            var position by remember { mutableStateOf(Offset.Zero) }
            val colorPicker =
                remember(dimensions) { ColorPickerCanvas(dimensions.first, dimensions.second) }

            var hasInput by remember { mutableStateOf(false) }
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
                    hasInput = true
                    updateColorPicker(down.position)
                    drag(down.id) { change ->
                        change.consume()
                        updateColorPicker(change.position)
                    }
                    hasInput = false
                }
            }

            Box(Modifier.fillMaxSize()) {
                Image(
                    modifier = inputModifier,
                    contentDescription = null,
                    bitmap = colorPicker.image
                )
                val color = colorPicker.colorForPosition(position)
                Magnifier(visible = true, position = position, color = color)
            }
        }
    }
}

/**
 * Magnifier displayed on top of [position] with the currently selected [color].
 */
@Composable
private fun Magnifier(visible: Boolean, position: Offset, color: Color) {
    val offset = with(LocalDensity.current) {
        Modifier.offset(
            position.x.toDp() - MagnifierWidth / 2,
            // Align with the center of the selection circle
            position.y.toDp() - MagnifierHeight / 2
        )
    }
    MagnifierTransition(
        visible,
        MagnifierWidth,
        SelectionCircleDiameter
    ) { labelWidth: Dp, selectionDiameter: Dp,
        alpha: Float ->
        Column(
            offset
                .size(width = MagnifierWidth, height = MagnifierHeight)
                .alpha(alpha)
        ) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CustomMagnifier(width = MagnifierWidth, height = MagnifierHeight, color = color)
            }
            Spacer(Modifier.height(20.dp))
//            Box(
//                Modifier
//                    .fillMaxWidth()
//                    .height(SelectionCircleDiameter),
//                contentAlignment = Alignment.Center
//            ) {
//                MagnifierSelectionCircle(Modifier.size(selectionDiameter))
//            }


//            Box(
//                Modifier
//                    .width(MagnifierWidth)
//                    .height(MagnifierHeight),
//                contentAlignment = Alignment.Center
//            ) {
//                //MagnifierLabel(Modifier.size(labelWidth, MagnifierLabelHeight), color)
//                CustomMagnifier(width = MagnifierWidth, height = MagnifierHeight, color = color)
//            }
        }
    }
}


/**
 * [Transition] that animates between [visible] states of the magnifier by animating the width of
 * the label, diameter of the selection circle, and alpha of the overall magnifier
 */
@Composable
private fun MagnifierTransition(
    visible: Boolean,
    maxWidth: Dp,
    maxDiameter: Dp,
    content: @Composable (labelWidth: Dp, selectionDiameter: Dp, alpha: Float) -> Unit
) {
    val transition = updateTransition(visible, label = "")
    val labelWidth by transition.animateDp(transitionSpec = { tween() }, label = "") {
        if (it) maxWidth else 0.dp
    }
    val magnifierDiameter by transition.animateDp(transitionSpec = { tween() }, label = "") {
        if (it) maxDiameter else 0.dp
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
 * Label representing the currently selected [color], with [Text] representing the hex code and a
 * square at the start showing the [color].
 */
@Composable
private fun MagnifierLabel(modifier: Modifier, color: Color) {
    Surface(shape = MagnifierPopupShape) {
        Row(modifier) {
            Box(
                Modifier
                    .weight(0.25f)
                    .fillMaxHeight()
                    .background(color)
            )
            // Add `#` and drop alpha characters
            val text = "#" + Integer.toHexString(color.toArgb()).uppercase(Locale.ROOT).drop(2)
            val textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
            Text(
                text = text,
                modifier = Modifier
                    .weight(0.75f)
                    .padding(top = 10.dp, bottom = 20.dp),
                style = textStyle,
                maxLines = 1
            )
        }
    }
}

/**
 * Selection circle drawn over the currently selected pixel of the color wheel.
 */
@Composable
private fun MagnifierSelectionCircle(modifier: Modifier) {
    Surface(
        modifier,
        shape = CircleShape,
        color = Color.Transparent,
        border = BorderStroke(2.dp, SolidColor(Color.Black.copy(alpha = 1f))),
        content = {}
    )
}

/**
 * A [GenericShape] that draws a box with a triangle at the bottom center to indicate a popup.
 */
private val MagnifierPopupShape = GenericShape { size, _ ->
    val width = size.width
    val height = size.height

    val arrowY = height * 0.8f
    val arrowXOffset = width * 0.4f

    addRoundRect(RoundRect(0f, 0f, width, arrowY, cornerRadius = CornerRadius(20f, 20f)))

    moveTo(arrowXOffset, arrowY)
    lineTo(width / 2f, height)
    lineTo(width - arrowXOffset, arrowY)
    close()
}


/**
 * A color wheel with an [ImageBitmap] that draws a circular color wheel of the specified diameter.
 */
private class ColorPickerCanvas(width: Int, height: Int) {

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
        center = Offset(width.toFloat() / 2, height.toFloat() / 2)
    )

    val image = ImageBitmap(width, height).also { imageBitmap ->
        val canvas = Canvas(imageBitmap)
        val paint = Paint().apply { shader = sweepGradient}
        canvas.drawRect(0F, 0F, width.toFloat(), height.toFloat(), paint)
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun MagnifierTest() {
    CustomMagnifier(60.dp, 100.dp, Color.Cyan)
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun ColorPickerDemoPreview() {
    ColorPickerTheme {
        ColorPickerDemo()
    }
}