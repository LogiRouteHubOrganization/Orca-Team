package edu.logiroute.logiroute.presentation.preview

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import edu.logiroute.logiroute.ui.WarehouseOperationalOverview
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import edu.logiroute.logiroute.fakeData.WarehouseSampleData

private val JetBlack = Color(0xFF0D141C)

@Preview
@Composable
fun WarehouseHeavyDataPreview() {
    val heavyWarehouse = WarehouseSampleData.warehouse12

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JetBlack)
    ) {
        WarehouseOperationalOverview(warehouse = heavyWarehouse)
    }
}

@Preview
@Composable
fun WarehouseOverloadedPreview() {
    val overloadedWarehouse = WarehouseSampleData.warehouse15
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JetBlack)
    ) {
        WarehouseOperationalOverview(warehouse = overloadedWarehouse)
    }
}


@Preview
@Composable
fun WarehouseZeroStatePreview() {
    val emptyWarehouse = WarehouseSampleData.warehouse50

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JetBlack)
    ) {
        WarehouseOperationalOverview(warehouse = emptyWarehouse)
    }
}

fun main() = application {
    val windowState = rememberWindowState(width = 1200.dp, height = 800.dp)

    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "LogiRoute - Warehouse Operational Overview Previews"
    ) {


        WarehouseOverloadedPreview()


    }
}
