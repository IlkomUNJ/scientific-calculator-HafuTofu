package com.example.basicskotlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.basicskotlin.ui.theme.BasicsKotlinTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Surface
import kotlin.math.max
import androidx.compose.ui.text.style.TextOverflow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BasicsKotlinTheme {
                MyApp(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun MyApp(modifier: Modifier = Modifier) {
    Surface(modifier = modifier) {
        CalculatorScreen()
    }
}


@Composable
private fun CalculatorScreen(
    modifier: Modifier = Modifier
) {
    val stateHolder = rememberSaveable(saver = CalculatorStateHolder.Saver) {
        CalculatorStateHolder()
    }

    val state = stateHolder.state
    val onAction = stateHolder::onAction

    val isScientificMode = state.isScientificMode

    val screenwidth = LocalConfiguration.current.screenWidthDp.dp
    val screenheight = LocalConfiguration.current.screenHeightDp.dp
    var buttonFontSize = (max(screenheight.value,screenwidth.value)/8*0.25).sp

    val rowWeight = 0.8f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 5.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.End,
    ) {
        Column(
            modifier = Modifier
                .weight(2f)
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom
        ) {
            val topDisplayContent = if (state.currentOperation.isNotEmpty() && state.currentOperation.contains('=')) {
                state.currentOperation
            } else if (state.result != "0" && state.result.isNotEmpty()) {
                state.result
            } else {
                "0"
            }

            Text(
                topDisplayContent,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                textAlign = TextAlign.End,
                fontSize = 25.sp,
                color = if (state.isError) androidx.compose.ui.graphics.Color.Red else androidx.compose.ui.graphics.Color.Gray,
                softWrap = false,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                state.result,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
                fontSize = 50.sp,
                color = if (state.isError) androidx.compose.ui.graphics.Color.Red else androidx.compose.ui.graphics.Color.Black,
                softWrap = false,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (!isScientificMode) {
            Row(
                modifier = Modifier
                    .weight(rowWeight)
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf("AC", "Del", "/").forEach { text ->
                    CalculatorButton(
                        text = text,
                        fontSize = buttonFontSize,
                        modifier = Modifier.weight(1.5f).fillMaxHeight(),
                        onClick = { onAction(getActionForButton(text)) }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .weight(rowWeight)
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf("1", "2", "3", "X").forEach { text ->
                    CalculatorButton(
                        text = text,
                        fontSize = buttonFontSize,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { onAction(getActionForButton(text)) }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .weight(rowWeight)
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf("4", "5", "6", "+").forEach { text ->
                    CalculatorButton(
                        text = text,
                        fontSize = buttonFontSize,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { onAction(getActionForButton(text)) }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .weight(rowWeight)
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf("7", "8", "9", "-").forEach { text ->
                    CalculatorButton(
                        text = text,
                        fontSize = buttonFontSize,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { onAction(getActionForButton(text)) }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .weight(rowWeight)
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf("SC","0",".","=").forEach { text ->
                    CalculatorButton(
                        text = text,
                        fontSize = buttonFontSize,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { onAction(getActionForButton(text)) }
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .weight(rowWeight)
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf("AC", "Del", "1/x", "/").forEach { text ->
                    CalculatorButton(
                        text = text,
                        fontSize = buttonFontSize,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { onAction(getActionForButton(text)) }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .weight(rowWeight)
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf("x!", "x^y", "sqrt(x)", "X").forEach { text ->
                    CalculatorButton(
                        text = text,
                        fontSize = buttonFontSize,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { onAction(getActionForButton(text)) }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .weight(rowWeight)
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf("sin", "cos", "tan", "+").forEach { text ->
                    CalculatorButton(
                        text = text,
                        fontSize = buttonFontSize,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { onAction(getActionForButton(text)) }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .weight(rowWeight)
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf("asin", "acos", "atan", "-").forEach { text ->
                    CalculatorButton(
                        text = text,
                        fontSize = buttonFontSize,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { onAction(getActionForButton(text)) }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .weight(rowWeight)
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf("123","log", "ln","=").forEach { text ->
                    CalculatorButton(
                        text = text,
                        fontSize = buttonFontSize,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { onAction(getActionForButton(text)) }
                    )
                }
            }
        }
    }
}

fun getActionForButton(text: String): CalculatorAction {
    return when (text) {
        "AC" -> CalculatorAction.Clear
        "Del" -> CalculatorAction.Delete
        "=" -> CalculatorAction.Calculate
        "+", "-", "/", "X", "." -> CalculatorAction.Operation(text)
        "SC" -> CalculatorAction.ToggleScientific 
        "123" -> CalculatorAction.ToggleScientific
        "1/x", "x!", "x^y", "sqrt(x)", "sin", "cos", "tan", "asin", "acos", "atan", "log", "ln" -> CalculatorAction.ScientificOperation(text)
        else -> CalculatorAction.Number(text.toIntOrNull() ?: 0) 
    }
}

@Composable
fun CalculatorButton(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit,
    onClick: () -> Unit
) {
    ElevatedButton(
        shape = RectangleShape,
        modifier = modifier.padding(horizontal = 2.dp, vertical = 2.dp),
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 5.dp)
    ) {
        Text(
            text,
            fontSize = fontSize,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview
@Composable
fun MyAppPreview() {
    BasicsKotlinTheme {
        MyApp(Modifier.fillMaxSize())
    }
}
