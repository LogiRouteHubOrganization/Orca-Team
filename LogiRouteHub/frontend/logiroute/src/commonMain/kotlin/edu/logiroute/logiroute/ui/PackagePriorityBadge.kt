package edu.logiroute.logiroute.ui


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import domain.model.Package


private val ErrorRed = Color(0xFFFFB4AB)
private val LightGreen=Color(0xFF92DE79)
private val CharcoalBlue=Color(0xFF27414E)
private val InkBlack = Color(0xFF121921)
private val TextSecondary = Color(0xFFA0A0A0)

@Composable
fun PackagePriorityBadge(
    packageItem: Package,
    modifier: Modifier = Modifier
) {
    val normalizedPriority=packageItem.priority.trim().uppercase()

    val (backgroundColor , textColor) =when (normalizedPriority) {
        "URGENT" , " CRITICAL" ->Pair(ErrorRed,InkBlack)
        "STANDARD"->Pair(LightGreen,InkBlack)
        "LOW"->Pair(CharcoalBlue,TextSecondary)
        else -> Pair(CharcoalBlue, TextSecondary)
    }
    Row(
       modifier=modifier
           .clip(RoundedCornerShape(6))
           .background(backgroundColor)
           .padding(horizontal = 100.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text=packageItem.id,
            color=textColor,
            fontSize=15.sp,
            fontWeight=  FontWeight.Bold
        )
        Text(
            text=".$normalizedPriority",
            color=textColor,
            fontSize=15.sp,
            fontWeight= FontWeight.Bold
        )
        Text(
            text="(${packageItem.weightByKg})Kg",
            color=TextSecondary,
            fontSize=15.sp,
            fontWeight= FontWeight.Bold
        )


    }

}


