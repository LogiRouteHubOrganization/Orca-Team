package edu.logiroute.logiroute.previews
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import edu.logiroute.logiroute.ui.WarehouseSummaryCard
import androidx.compose.ui.tooling.preview.Preview


@Composable
@Preview
fun WarehouseComponentPreviews(){
    Column(
        modifier = Modifier
            .background(Color.Black)
            .safeContentPadding()
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SampleData.sampleWarehouses.forEach { wh -> WarehouseSummaryCard(warehouse = wh)

        }
    }
}




