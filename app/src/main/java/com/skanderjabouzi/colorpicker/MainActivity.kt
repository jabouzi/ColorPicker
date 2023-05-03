package com.skanderjabouzi.colorpicker

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.skanderjabouzi.colorpicker.ui.theme.ColorPickerTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ColorPickerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ColorPickerDemo(400.dp, 800.dp)
                }
            }
        }
    }
}

@Composable
fun BulbSlot(
    content: @Composable () -> Unit
) {
    content()
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ColorPickerDemo(
    colorPickerwidth: Dp = 200.dp,
    colorPickerheight: Dp = 500.dp,
    magnifierWidth: Dp = 60.dp,
    magnifierHeight: Dp = 100.dp,
) {
    data class ButtonColor(var brightness: Color, val colorPicker: Color, val temperature: Color)
    data class SlotVisible(var brightness: Boolean, val colorPicker: Boolean, val temperature: Boolean)
    val buttonColor = ButtonColor(Color.Transparent, Color.Transparent, Color.Transparent)
    val slotVisible = SlotVisible(false, false, false)
    var selectedColor by remember { mutableStateOf(Color(0xFF83eb34)) }
    var positionOffset by remember { mutableStateOf(Offset(colorPickerwidth.value / 2, colorPickerheight.value / 2)) }
    var buttonColorState by remember { mutableStateOf(buttonColor) }
    var slotVisibleState by remember { mutableStateOf(slotVisible) }

    val brush = Brush.verticalGradient(
        colors = listOf(
            Color.White,
            selectedColor
        )
    )

    buttonColorState = buttonColor.copy(colorPicker = Color.Red)
    slotVisibleState = slotVisible.copy(colorPicker = true)

    ColorPickerTheme {
        Column(
            modifier = Modifier.fillMaxSize()
                .background(selectedColor),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            BulbSlot {
                if (slotVisibleState.brightness) {
                    Slider(
                        gradientColorBottom = selectedColor,
                    )
                }
            }

            BulbSlot{
                if (slotVisibleState.colorPicker) {
                    ColorPicker(
                        colorPickerWidth = 500.dp,
                        colorPickerHeight = 800.dp,
                        magnifierWidth = 60.dp,
                        magnifierHeight = 100.dp,
                        onColorChange = { selectedColor = it },
                        onPositionChange = { positionOffset = it }
                    )
                }
            }

            BulbSlot {
                if (slotVisibleState.temperature) {
                    Slider(
                        gradientColorBottom = selectedColor,
                        gradientColorTop = Color(0xFF9C27B0),
                        range = 0..25
                    )
                }
            }

            Row(
                modifier = Modifier.padding(5.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = {
                        slotVisibleState = slotVisible.copy(brightness = true)
                        buttonColorState = buttonColor.copy(brightness = Color.Red)
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .border(1.dp, buttonColorState.brightness, shape = CircleShape)
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
                        slotVisibleState = slotVisible.copy(colorPicker = true)
                        buttonColorState = buttonColor.copy(colorPicker = Color.Red)
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .border(1.dp, buttonColorState.colorPicker, shape = CircleShape)
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

                GradientButton(
                    text = "",
                    textColor = Color.Unspecified,
                    size = 50.dp,
                    borderColor = buttonColorState.temperature,
                    borderWidth = 1.dp,
                    gradient = brush,
                    onClick = {
                        slotVisibleState = slotVisible.copy(temperature = true)
                        buttonColorState = buttonColor.copy(temperature = Color.Red)
                    }
                )
            }
        }
    }
}

@Composable
fun GradientButton(
    text: String,
    textColor: Color,
    size: Dp,
    borderColor: Color,
    borderWidth: Dp,
    gradient: Brush,
    onClick: () -> Unit,
) {
    Button(
        modifier = Modifier
            .size(size)
            .border(borderWidth, borderColor, shape = CircleShape),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(),
        onClick = { onClick() })
    {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape = CircleShape)
                .background(gradient)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = text, color = textColor)
        }
    }
}


fun GradientBrush(
    size: Float,
    colorsList: List<Color>
): Brush {
    return Brush.sweepGradient(
        colors = colorsList,
        center = Offset(x = size / 2, y = size / 2)
    )
}

val largeRadialGradient = object : ShaderBrush() {
    override fun createShader(size: Size): Shader {
        val biggerDimension = maxOf(size.height, size.width)
        return RadialGradientShader(
            colors = listOf(
                Color.Red,
                Color.Magenta,
                Color.Blue,
                Color.Cyan,
                Color.Green,
                Color.Yellow,
                Color.Red
            ),
            center = size.center,
            radius = biggerDimension / 2f,
            colorStops = listOf(0f, 0.95f)
        )
    }
}

@Preview
@Composable
fun GradientButtonPreview() {
    val brush = GradientBrush(
        size = 50F,
        colorsList = listOf(
            Color.Red,
            Color.Magenta,
            Color.Blue,
            Color.Cyan,
            Color.Green,
            Color.Yellow,
            Color.Red
        )
    )
    GradientButton(
        text = "",
        textColor = Color.Unspecified,
        size = 50.dp,
        borderColor = Color.Red,
        borderWidth = 1.dp,
        gradient = largeRadialGradient,
        onClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview2() {
    ColorPickerTheme {
        ColorPickerDemo2()
    }
}

@Preview(showBackground = true)
@Composable
private fun DraggableTextLowLevel() {
    Box(modifier = Modifier.fillMaxSize()) {
        var offsetX by remember { mutableStateOf(0f) }
        var offsetY by remember { mutableStateOf(0f) }

        println("offsetX: $offsetX, offsetY: $offsetY")

        Box(
            Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .background(Color.Blue)
                .size(50.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                }
        )
    }
}
