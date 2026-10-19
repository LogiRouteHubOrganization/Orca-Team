package edu.logiroute.logiroute.ui

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import domain.model.Warehouse
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import edu.logiroute.logiroute.theme.WarningAmber
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement

@Composable
fun WarehouseOperationalOverview(
    warehouse: Warehouse,
    modifier:Modifier = Modifier
){
    //الحين هين اول سطر في البند الثاني هين حسبت مجموع اوزان لبكجات في الطابور هادي اول عملية ثاني عملية الي هي الماكس اللي هي سعة القصوى اللي في بكيجات  الي عندي اكبر قيمة عندي بعدين قارنت بناتهم بعدين جبت فرق بناتهم
    val totalQueue=warehouse.cargoQueue.sumOf { it.weightByKg }
    val totalStationedFleet =warehouse.stationedVehicles.sumOf { it.maxCapacityKg }
    val isOverloaded=totalQueue>totalStationedFleet
    val excessWeight=totalQueue-totalStationedFleet
    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ){
            //الحين هينا شو الي صار هين طلب منا انو نشوف اذ وزن الي محمل اكبر يعني ناتج راح يطلع ترو ف لما ناتج يطلع ترو شو يعمل يروح يطلع مربع في جملة تحذيرة انو والل في عندك سعة ممتلى بلون الاصفر ف لما يطلع الرسالة التحذيرة بتطلع فوق وحطيتها في بوكس و في الو الستايل الخاص فيه
            if (isOverloaded){
                Box (
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(WarningAmber)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ){
                    BasicText(
                        text="⚠️ Warehouse Overloaded: Cargo queue weight exceeds stationed fleet capacity by [$excessWeight] kg.",
                        style = TextStyle(
                            color = WarningAmber,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
            //هينا شو اللي صار انو الحين بدي اعمل اكثر من خلفة لجوال ولاب يعني الحين انا اول اشي حطيت للاب وحددت لكل واحدة من الكومبوز اللي عندي مقدار معين من الاحداثايات يعني الحين الكومبوز الاول مثلا 40 في مية من شاشة والشاشة الثانية او الكومبوز القاني حطلي انم 60 ف هين انا حددت القيمة اما لما كتبت الس هادي يعني لو ما حددت القيمة او معيار اللي بتكون اكبر منوا  اما لما كتبت الس هادي يعني انو  لجوال وقيمة محددة انها كلها
            WarehouseSummaryCard(warehouse = warehouse)
            if(maxWidth >= 840.dp){
                Row(Modifier
                    .fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ){
                    WarehouseCargoQueueList(
                        cargoQueue = warehouse.cargoQueue,
                        modifier = Modifier.weight(0.4f)
                    )

                    ActiveFleetGrid(
                        vehicles = warehouse.stationedVehicles,
                        modifier = Modifier.weight(0.6f)
                    )

                }
            }
            else{
                Column (Modifier
                    .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    WarehouseCargoQueueList(
                        cargoQueue = warehouse.cargoQueue,
                        modifier = Modifier.weight(1f)
                    )

                    ActiveFleetGrid(
                        vehicles = warehouse.stationedVehicles,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }






}