package edu.logiroute.logiroute.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import domain.model.Vehicle
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import  androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import edu.logiroute.logiroute.fakeData.PackageSampleData
import edu.logiroute.logiroute.fakeData.VehicleSampleData
import edu.logiroute.logiroute.theme.ErrorRed
import logiroutehub.frontend.logiroute.generated.resources.Res
import logiroutehub.frontend.logiroute.generated.resources.byteloom_logo
import org.jetbrains.compose.resources.painterResource


@Composable
fun ActiveFleetGrid(
    vehicles: List<Vehicle>,
    vehicleLoads: Map<String, Double> = emptyMap(),
    modifier: Modifier = Modifier
){
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 280.dp),
        modifier=modifier
            .fillMaxSize(),
        contentPadding=PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ){
        items(
            items =vehicles,
            key = { it.id }
        ){ vehicle ->
            //الحين اللي صار هين انو طلب منوا يجيب  الي الوزن بس مش كلهم في الهم وزن ف شو االي بعملو انا اني بجيب الابي تبع الفكل بعدين اذ في اي بدي والله يعني في  وزن اذ فش ابي يعني فش وزن ف روح اعطي الوزن تبعوا انو هو 0.0 انو فش  وزن الو
             val currentLoad=vehicleLoads[vehicle.id]?:0.0
            //الحين هين شو اللي صار الحين هو بدوا النسبة تبعت الماكس  ف هين بدي تاكد انو الماكس تيع الفكيل اصلا فش عندي يعني باول بدي تاكد انو اللي عندي مش صفر ف عشان هيك بدي اتحقق من الشرط باول بعدين بدي اعمل عملية الحسابية اللي راح احتاجها في الشرط الي تحت يعني الحين ضرب في مية عشان انا بدي نسبة مئوية  غير ذالك ارجع الي بصفر
             val  ratio=
                 if(vehicle.maxCapacityKg>0.0) {
                     (currentLoad/vehicle.maxCapacityKg)*100
                 }else
                     0.0
            //الحين هينا طلب منا انو لو النسبة طلعت اكبر من خمسة وثمانين بدك تروح تعمل بورد هاد البورد  بلون معين وبشكل معين وغير ذالك انو فش عندي بورد ف خلص خليه زي ما هو
             val warningBorderModifier=  if(ratio>85.0){
                Modifier.border(
                    width = 2.dp,
                    color = ErrorRed)
            }else
                Modifier
            //الفكرة اللي هنا انو الحين بعد ما عرفت انو لو فوق 85 خلص روح اعمل اطار طيب حلو شو اللي بدوا يصير الحين بعد ما عرفت انو هي اكبر 85 انا اعطيت اطار طيب هاد الاطار بدي امرر الشرط اللي انا حطيتوا لمين ل الكومبوز الي انا معرفاه الو ف بس والله وكلهم طبعا في ايتمز اللي هي تبعت LazyVerticalGrid
            VehicleDetailCard(
                modifier=warningBorderModifier,
                vehicle=vehicle,
                currentLoadKg = currentLoad
            )


        }
    }
}
fun main() = application {
    val windowState = rememberWindowState(width = 1200.dp, height = 800.dp)

    Window(
        onCloseRequest = ::exitApplication,
        title = "ByteBloom LogiRoute",
        resizable = true,
        icon = painterResource(Res.drawable.byteloom_logo
        )
    ) {
        ActiveFleetGrid(VehicleSampleData.AllVehicle)

    }
}
