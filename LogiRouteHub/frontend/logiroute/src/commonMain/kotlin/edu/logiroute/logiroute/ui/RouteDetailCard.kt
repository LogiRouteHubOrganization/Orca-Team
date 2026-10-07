package edu.logiroute.logiroute.ui
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import domain.model.Route
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import edu.logiroute.logiroute.theme.TextPrimary
import edu.logiroute.logiroute.theme.icon.ROUTE_ARROW
import edu.logiroute.logiroute.theme.SapphireSky
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import edu.logiroute.logiroute.theme.Border
import edu.logiroute.logiroute.theme.CharcoalBlue
import androidx.compose.foundation.clickable
import edu.logiroute.logiroute.theme.TextSecondary
import edu.logiroute.logiroute.theme.WarningAmber

@Composable
fun RouteDetailCard(
    route: Route,
    modifier:Modifier=Modifier,
    onClick: (Route) -> Unit = {}
){
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
//            .fillMaxWidth()
            .clip(shape)
            .background(CharcoalBlue)
            .border(
                width = 1.dp,
                color = Border,
                shape = shape)
            .clickable { onClick(route) }
            .padding(16.dp)

    ){
        Row(
            modifier=Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement =Arrangement.SpaceBetween

        ){
            Text (
                text=route.originWarehouse.name,
                color= TextPrimary ,
                fontWeight= FontWeight.Bold
            )

            Text(
                text = ROUTE_ARROW,
                color = SapphireSky,
                fontWeight = FontWeight.Bold
            )

            Text(
                text=route.destinationWarehouse.name,
                color=TextPrimary,
                fontWeight= FontWeight.Bold

            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier=Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement =Arrangement.SpaceBetween
        ){
            Text(
                text="${route.distanceKm} km",
                color = TextSecondary
            )

            val delayColor=
                if(route.typicalDelayMin>60)
                    WarningAmber
                else
                    TextSecondary
            Text(
                text="${route.typicalDelayMin} min delay",
                color = delayColor

            )
        }


    }

}


@Preview
@Composable
fun  RouteDetailCardpreview(){}
