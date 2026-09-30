package edu.logiroute.logiroute.ui
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import domain.model.Warehouse
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import edu.logiroute.logiroute.fakeData.SampleData
import edu.logiroute.logiroute.theme.Platinum
import edu.logiroute.logiroute.theme.QuickSilver
import edu.logiroute.logiroute.theme.SapphireSky
import edu.logiroute.logiroute.theme.CyberSprout

@Composable
fun WarehouseIdentityBadge(
    warehouse: Warehouse,
    modifier: Modifier = Modifier,
    onClick : (() -> Unit)? = null
){
    Row(
        modifier=modifier
            .clip(RoundedCornerShape(6))
            .clickable{onClick?.invoke()}
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(100.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        Text(
            text=warehouse.id,
            fontFamily = FontFamily.Monospace ,
            fontSize = 12.sp,
            color = QuickSilver,
        )
        Text(
            text= warehouse.name,
            color = Platinum,
            fontWeight=  FontWeight.Bold
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(SapphireSky)
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ){
            Text(
                text= warehouse.regionalZone,
                color=CyberSprout
            )
        }

    }

}

@Preview(showBackground = true)
@Composable
fun WarehouseIdentityBadgePreviews(){
    Column(
        modifier = Modifier
            .background(Color.Black)
            .safeContentPadding()
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SampleData.sampleWarehouses.forEach { wh ->
            WarehouseSummaryCard(warehouse = wh)

        }
    }
}