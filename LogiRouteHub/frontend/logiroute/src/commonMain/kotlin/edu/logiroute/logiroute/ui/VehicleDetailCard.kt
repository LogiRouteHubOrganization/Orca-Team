package edu.logiroute.logiroute.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import domain.model.Vehicle
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import edu.logiroute.logiroute.theme.CharcoalBlue
import edu.logiroute.logiroute.theme.Border
import edu.logiroute.logiroute.theme.TextPrimary
import edu.logiroute.logiroute.theme.CyberSprout
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import edu.logiroute.logiroute.theme.LightGreen
import androidx.compose.ui.unit.sp
import edu.logiroute.logiroute.theme.TextSecondary

@Composable
fun VehicleDetailCard(
    vehicle: Vehicle,
    currentLoadKg: Double = 0.0,
    modifier : Modifier =Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CharcoalBlue)
            .border(
                width = 1.dp,
                color = Border,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ){
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicText(
                text = vehicle.id,
                style = TextStyle(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12))
                    .border(
                        width = 1.dp,
                        color = CyberSprout,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)

            ) {
                BasicText(
                    text = vehicle.currentWarehouse.name
                )
            }
        }
        BasicText(
            text= buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        color = LightGreen,
                        fontWeight = FontWeight.Bold
                    )){
                    append("${vehicle.costPerKm}")
                }
                withStyle(
                    SpanStyle(
                        color=TextSecondary
                    )){
                    append("/km")
                }
                withStyle(
                    SpanStyle(
                        color = LightGreen,
                        fontWeight = FontWeight.Bold
                    )
                ){
                    append("${vehicle.maxCapacityKg}")
                }
                withStyle(
                    SpanStyle(
                        color=TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                ){
                    append("kg")
                }

            }
        )

        VehicleCapacityIndicator(
            currentLoadKg= currentLoadKg,
            maxCapacityKg= vehicle.maxCapacityKg
        )
    }

}



@Preview
@Composable
fun VehicleDetailCardpreview(){
}
