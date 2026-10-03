package edu.logiroute.logiroute.ui
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.logiroute.logiroute.theme.*
import kotlin.math.roundToInt
import androidx.compose.ui.text.TextStyle

@Composable
fun VehicleCapacityIndicator(
    currentLoadKg: Double,
    maxCapacityKg: Double,
    modifier: Modifier = Modifier
) {
    val safeMax = maxCapacityKg.coerceAtLeast(0.0001)
    val ratio = (currentLoadKg / safeMax)
        .toFloat()
        .coerceIn(0.0f, 1.0f)

    val fillColor = when {
        ratio < 0.70f -> LightGreen
            ratio <= 0.90f -> WarningAmber
        else -> ErrorRed
    }
    val percentage = (ratio * 100).roundToInt()

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .drawBehind {
                    val radius = CornerRadius(size.height / 2, size.height / 2)

                    drawRoundRect(
                        color = JetBlack,
                        size = size,
                        cornerRadius = radius
                    )

                    drawRoundRect(
                        color = fillColor,
                        size = Size(width = size.width * ratio, height = size.height),
                        cornerRadius = radius
                    )

                }
        )
        BasicText(
            text="${percentage}%",
            style = TextStyle(
                color = TextSecondary,
                textAlign = TextAlign.Center,
                fontSize = 12.sp

            )
        )


    }
}

@Preview
@Composable
fun VehicleCapacityIndicatorpreview() {
    Column(
        modifier = Modifier
            .safeContentPadding()
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        VehicleCapacityIndicator(
            currentLoadKg = 8400.0,
            maxCapacityKg = 10000.0
        )
    }
}



