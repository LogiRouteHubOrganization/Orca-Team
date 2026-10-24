package edu.logiroute.logiroute.ui


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import domain.model.Package
import edu.logiroute.logiroute.theme.CharcoalBlue
import edu.logiroute.logiroute.theme.ErrorRed
import edu.logiroute.logiroute.theme.LightGreen
import edu.logiroute.logiroute.theme.InkBlack
import edu.logiroute.logiroute.theme.TextSecondary
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import edu.logiroute.logiroute.fakeData.PackageSampleData

private data class BadgeColors(val background :Color,val text :Color )

@Composable
fun PackagePriorityBadge(
    packageItem: Package,
    modifier: Modifier = Modifier

) {
    val normalizedPriority=packageItem.priority.trim().uppercase()

    val colors =when (normalizedPriority) {
        "URGENT" , " CRITICAL" ->BadgeColors(ErrorRed,InkBlack)
        "STANDARD"->BadgeColors(LightGreen,InkBlack)
        "LOW"->BadgeColors(CharcoalBlue,TextSecondary)
        else -> BadgeColors(CharcoalBlue, TextSecondary)
    }
    Row(
       modifier=modifier
           .clip(RoundedCornerShape(6))
           .background(colors.background)
           .padding(horizontal = 100.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text=packageItem.id,
            color=colors.text,
            fontSize=15.sp,
            fontWeight=  FontWeight.Bold
        )
        Text(
            text=".$normalizedPriority",
            color=colors.text,
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

@Preview
@Composable
fun  PackagePriorityBadgepreview() {
    Column(
        modifier = Modifier
            .background(Color.Black)
            .safeContentPadding()
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    )
    {
        PackageSampleData.AllPackage.forEach { pkg ->
            PackagePriorityBadge(packageItem = pkg)
        }

        }
    }


