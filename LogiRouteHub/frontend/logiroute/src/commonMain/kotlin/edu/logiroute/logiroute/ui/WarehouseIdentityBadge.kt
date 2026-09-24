package edu.logiroute.logiroute.ui
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import domain.model.Warehouse
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp


private val Platinum= Color(0xFFE0E0E0)
private val QuickSilver=Color(0xFFA0A0A0)
private val SapphireSky=Color(0xFF316EBF)
private val CyberSprout=Color(0xFF73D7B5)

@Composable
fun WarehouseIdentityBadge(
    warehouse: Warehouse,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {

    }
){
    Row(
        modifier=modifier
            .clip(RoundedCornerShape(6))
            .clickable(onClick=onClick)
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