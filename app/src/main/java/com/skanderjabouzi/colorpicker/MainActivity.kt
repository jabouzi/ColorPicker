package com.skanderjabouzi.colorpicker

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.skanderjabouzi.colorpicker.ui.theme.ColorPickerTheme

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
    selectionCircleDiameter: Dp = 30.dp,
) {
    data class ButtonColor(var brightness: Color, val colorPicker: Color, val temperature: Color)
    data class SlotVisible(var brightness: Boolean, val colorPicker: Boolean, val temperature: Boolean)
    val buttonColor = ButtonColor(Color.Transparent, Color.Transparent, Color.Transparent)
    val slotVisible = SlotVisible(false, false, false)
    var selectedColor by remember { mutableStateOf(Color(0xFF83eb34)) }
    var buttonColorState by remember { mutableStateOf(buttonColor) }
    var slotVisibleState by remember { mutableStateOf(slotVisible) }

    buttonColorState = buttonColor.copy(colorPicker = Color.Red)
    slotVisibleState = slotVisible.copy(colorPicker = true)

    ColorPickerTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
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
                        selectionCircleDiameter = 30.dp,
                        onColorChange = { selectedColor = it }
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

                Button(
                    onClick = {
                        slotVisibleState = slotVisible.copy(temperature = true)
                        buttonColorState = buttonColor.copy(temperature = Color.Red)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = selectedColor),
                    modifier = Modifier
                        .size(50.dp)
                        .border(1.dp, buttonColorState.temperature, shape = CircleShape)
                ) {
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview2() {
    ColorPickerTheme {
        ColorPickerDemo2()
    }
}