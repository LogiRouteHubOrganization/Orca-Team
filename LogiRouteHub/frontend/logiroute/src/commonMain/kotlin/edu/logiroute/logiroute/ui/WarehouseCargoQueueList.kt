package edu.logiroute.logiroute.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import domain.model.Package
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.draw.clip
import edu.logiroute.logiroute.theme.InkBlack
import edu.logiroute.logiroute.theme.Border
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import edu.logiroute.logiroute.theme.TextPrimary
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.fillMaxSize


@Composable
fun WarehouseCargoQueueList (
    cargoQueue: List<Package>,
    modifier: Modifier = Modifier
) {
    if(cargoQueue.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            BasicText(
                text = "No Pending Cargo",
            )
        }
        return
    }
        val priorityOrder = listOf("URGENT", "STANDARD", "LOW")

        val groupedPackages = cargoQueue
            .groupBy { it.priority.uppercase() }

        LazyColumn(
            modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            //هين اللي صا رالحين انا بدي اعرض الاولية تبعت البكيج مع عدد البكيات انا اساس عملت ليست وحددت الاولولة بناء على الاولوية ف بستخدم نغس هادي اليست وبمررها على فور ايشت يمرر الها الاولوية والبكيات الحين بمرر ل ستاك هيدر الاولية اليل انا اصلا بدي اعمل الها عرض ف الحين جوا الستاك هيدر بكتب بوكس ليش عشان بدي اهرض هاد الهيدر في بوكس وحطيت اللون المطلوب وحطيت باك القراوند ولون السفلي ل البورد بعدين بدي اكتب شو هو هيدر اللي هو الاولوية وعدد البكيجات وحطيت اللون ز بولد
            groupedPackages.forEach { (priority, packages) ->
                stickyHeader(priority) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .fillMaxWidth()
                            .background(color = InkBlack)
                            .drawBehind {
                                val strokeWidth = 1.dp.toPx()
                                drawLine(
                                    color = Border,
                                    start = Offset(0f, size.height),
                                    end = Offset(size.width, size.height),
                                    strokeWidth = strokeWidth
                                )
                            }
                    ) {
                        BasicText(
                            text = "$priority CARGO (${packages.size})",
                            style = TextStyle(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )

                    }
                }


                items(
                    items = packages,
                    key = { it.id },
                    contentType = { "PackageItem" }
                ) { packageItem ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PackagePriorityBadge(packageItem = packageItem)

                        Column {
                            BasicText(
                                text = packageItem.destinationWarehouse.name,
                            )
                            BasicText(
                                text = "${packageItem.weightByKg} kg"
                            )
                        }
                    }
                }

            }
        }
    }
