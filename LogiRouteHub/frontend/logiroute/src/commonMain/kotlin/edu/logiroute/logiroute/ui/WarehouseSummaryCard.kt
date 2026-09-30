package edu.logiroute.logiroute.ui
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import domain.model.Warehouse
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import edu.logiroute.logiroute.fakeData.SampleData
import edu.logiroute.logiroute.theme.JetBlack
import edu.logiroute.logiroute.theme.borderColor
import edu.logiroute.logiroute.theme.textPrimary
import edu.logiroute.logiroute.theme.Scorpion

private fun priorityWeight(priority: String): Int {
    return when (priority.trim().uppercase()) {
        "URGENT", "CRITICAL" -> 3
        "STANDARD" -> 2
        "LOW" -> 1
        else -> 0
    }
}

@Composable
fun WarehouseSummaryCard(
    warehouse: Warehouse,
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier
            .fillMaxWidth()
        .clip(RoundedCornerShape(10.dp))
        .background(JetBlack)
        .padding(horizontal = 8.dp, vertical = 4.dp)
        .border(1.dp,borderColor,RoundedCornerShape(10.dp)),
        verticalArrangement = Arrangement.spacedBy(8.dp)    )
    {
        WarehouseIdentityBadge(warehouse = warehouse)
        Row(
            modifier =modifier
                .fillMaxWidth()
                .padding(vertical = 30.dp),
            horizontalArrangement = Arrangement.spacedBy(1000.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(
                text = "Queue Package = (${warehouse.cargoQueue.size})",
                color = textPrimary

            )
            Text(
                text="stationed Vehicles = (${warehouse.stationedVehicles.size})",
                color = textPrimary
            )
        }


        if (warehouse.cargoQueue.isNotEmpty()) {
            val topPackage = warehouse.cargoQueue.maxByOrNull { priorityWeight(it.priority) }
            topPackage?.let {
                PackagePriorityBadge(packageItem = it)
            }
        } else {
            Text(
                text = "No Pending Cargo",
                color = Scorpion
            )
        }
    }

}

@Preview(showBackground = true)
@Composable
fun WarehouseComponentPreviews(){
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

