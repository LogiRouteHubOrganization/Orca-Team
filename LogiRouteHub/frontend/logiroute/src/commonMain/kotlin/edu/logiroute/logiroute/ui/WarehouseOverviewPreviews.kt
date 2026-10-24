package edu.logiroute.logiroute.presentation.preview

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import edu.logiroute.logiroute.ui.WarehouseOperationalOverview
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import edu.logiroute.logiroute.fakeData.PackageSampleData.package31
import edu.logiroute.logiroute.fakeData.VehicleSampleData.vehicle12
import edu.logiroute.logiroute.fakeData.WarehouseSampleData
import logiroutehub.frontend.logiroute.generated.resources.Res
import logiroutehub.frontend.logiroute.generated.resources.byteloom_logo
import org.jetbrains.compose.resources.painterResource
import edu.logiroute.logiroute.fakeData.PackageSampleData.package3
import edu.logiroute.logiroute.fakeData.VehicleSampleData.vehicle11


@Preview
@Composable
fun WarehouseHeavyDataPreview() {
    val testWarehouse1 = WarehouseSampleData.warehouse50.apply {
        addPackage(package31)
        addVehicle(vehicle12) }
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        WarehouseOperationalOverview(warehouse = testWarehouse1)
    }
}

@Preview
@Composable
fun WarehouseOverloadedPreview() {

    val overloadedWarehouse2 = WarehouseSampleData.warehouse12.apply {
        addPackage(package3)
        addVehicle(vehicle11)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        WarehouseOperationalOverview(warehouse = overloadedWarehouse2)
    }
}


@Preview
@Composable
fun WarehouseZeroStatePreview() {
    val emptyWarehouse =  WarehouseSampleData.warehouse22.apply {}

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        WarehouseOperationalOverview(warehouse = emptyWarehouse)
    }
}

fun main() = application {

    Window(
        onCloseRequest = ::exitApplication,
        title = "ByteBloom LogiRoute",
        resizable = true,
        icon = painterResource(Res.drawable.byteloom_logo)
    ) {

        //WarehouseHeavyDataPreview()
        WarehouseOverloadedPreview()
        //WarehouseZeroStatePreview()


    }
}
