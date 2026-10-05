package edu.logiroute.logiroute.preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import edu.logiroute.logiroute.ui.VehicleDetailCard
import edu.logiroute.logiroute.fakeData.VehicleSampleData
import edu.logiroute.logiroute.ui.RouteDetailCard
import edu.logiroute.logiroute.fakeData.RouteSampleData

@Preview
@Composable
fun SafeLoadState(){
    VehicleDetailCard(
        VehicleSampleData.vehicle2,
        2500.0
    )
}

@Preview
@Composable
fun HeavyLoadWarningState(){
    VehicleDetailCard(
        VehicleSampleData.vehicle2,
        8200.0
    )
}

@Preview
@Composable
fun OverLoadedAlertState(){
    VehicleDetailCard(
        VehicleSampleData.vehicle3,
        10500.0
    )
}

@Preview
@Composable
fun ShortLocalTransit(){
    RouteDetailCard(
        RouteSampleData.route3
    )
}
@Preview
@Composable
fun LongCrossCountryTransit(){
    RouteDetailCard(
        RouteSampleData.route2
    )
}



