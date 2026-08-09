package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.EmployeeAvatar
import com.example.ui.components.StatCard
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError
import com.example.ui.viewmodel.MonthlyReportSummary
import com.example.util.MonthItem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.filled.BarChart
import com.example.ui.viewmodel.MonthTrendItem

@Composable
fun AdminReportsScreen(
    past6Months: List<MonthItem>,
    selectedMonthKey: String,
    reportSummary: MonthlyReportSummary?,
    sixMonthTrendList: List<MonthTrendItem> = emptyList(),
    onSelectMonth: (String) -> Unit,
    onShowToast: (String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_reports_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 6-Month Visual Bar Chart Section
        item {
            SixMonthAttendanceGraphCard(
                trendList = sixMonthTrendList,
                selectedMonthKey = selectedMonthKey,
                onSelectMonth = onSelectMonth
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Automated Monthly Reports",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                if (reportSummary != null) {
                                    val text = buildString {
                                        appendLine("📊 ATTENDEASE MONTHLY ATTENDANCE REPORT")
                                        appendLine("Month: ${reportSummary.monthItem.displayName}")
                                        appendLine("Working Days: ${reportSummary.totalWorkingDays}")
                                        appendLine("Total Staff: ${reportSummary.totalEmployees}")
                                        appendLine("Average Attendance Rate: ${String.format("%.1f", reportSummary.averageAttendanceRatePct)}%")
                                        appendLine("Total Present: ${reportSummary.totalPresentLogs}")
                                        appendLine("Total Late: ${reportSummary.totalLateLogs}")
                                        appendLine("Total Leaves: ${reportSummary.totalLeaveLogs}")
                                        appendLine("\nPER-EMPLOYEE BREAKDOWN:")
                                        reportSummary.employeeSummaries.forEach { emp ->
                                            appendLine("- ${emp.employee.name} (${emp.employee.department}): Present ${emp.presentDays}d, Late ${emp.lateDays}d, Leaves ${emp.leaveDays}d [${String.format("%.1f", emp.attendancePercentage)}%]")
                                        }
                                    }
                                    clipboardManager.setText(AnnotatedString(text))
                                    onShowToast("📋 Report for ${reportSummary.monthItem.displayName} copied to clipboard!")
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("copy_report_btn")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export Report", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Select Month (Last 6 Months):",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(past6Months) { month ->
                            val isSelected = month.yearMonthKey == selectedMonthKey
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectMonth(month.yearMonthKey) },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.DateRange,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(month.displayName, fontSize = 12.sp)
                                    }
                                },
                                modifier = Modifier.testTag("month_chip_${month.yearMonthKey}")
                            )
                        }
                    }
                }
            }
        }

        // Selected Month High-Level Stat Cards
        if (reportSummary != null) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Avg Attendance",
                            value = "${String.format("%.1f", reportSummary.averageAttendanceRatePct)}%",
                            icon = Icons.Default.Assessment,
                            iconBgColor = EmeraldSuccess.copy(alpha = 0.15f),
                            iconTintColor = EmeraldSuccess,
                            subtitle = "${reportSummary.totalWorkingDays} Work Days",
                            modifier = Modifier.weight(1f)
                        )

                        StatCard(
                            title = "Total Present",
                            value = "${reportSummary.totalPresentLogs}",
                            icon = Icons.Default.CheckCircle,
                            iconBgColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            iconTintColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Late Check-Ins",
                            value = "${reportSummary.totalLateLogs}",
                            icon = Icons.Default.MoreTime,
                            iconBgColor = AmberWarning.copy(alpha = 0.15f),
                            iconTintColor = AmberWarning,
                            modifier = Modifier.weight(1f)
                        )

                        StatCard(
                            title = "Leaves Taken",
                            value = "${reportSummary.totalLeaveLogs}",
                            icon = Icons.Default.EventBusy,
                            iconBgColor = RoseError.copy(alpha = 0.15f),
                            iconTintColor = RoseError,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Per-Employee Breakdown (${reportSummary.monthItem.displayName})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(reportSummary.employeeSummaries) { empSummary ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                EmployeeAvatar(
                                    name = empSummary.employee.name,
                                    photoUri = empSummary.employee.photoUri,
                                    size = 40.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = empSummary.employee.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${empSummary.employee.department} • ${empSummary.employee.employeeId}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            val pctColor = when {
                                empSummary.attendancePercentage >= 90f -> EmeraldSuccess
                                empSummary.attendancePercentage >= 75f -> AmberWarning
                                else -> RoseError
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(pctColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${String.format("%.1f", empSummary.attendancePercentage)}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = pctColor,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { (empSummary.attendancePercentage / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = when {
                                empSummary.attendancePercentage >= 90f -> EmeraldSuccess
                                empSummary.attendancePercentage >= 75f -> AmberWarning
                                else -> RoseError
                            },
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Present: ${empSummary.presentDays}d",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldSuccess
                            )
                            Text(
                                text = "Late: ${empSummary.lateDays}d",
                                style = MaterialTheme.typography.labelSmall,
                                color = AmberWarning
                            )
                            Text(
                                text = "Leaves: ${empSummary.leaveDays}d",
                                style = MaterialTheme.typography.labelSmall,
                                color = RoseError
                            )
                            Text(
                                text = "Work Days: ${empSummary.totalWorkingDays}d",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SixMonthAttendanceGraphCard(
    trendList: List<MonthTrendItem>,
    selectedMonthKey: String,
    onSelectMonth: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("six_month_graph_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "6-Month Attendance Trends",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap any bar to view detailed breakdown",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Graph canvas area with background grid lines
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                // Background grid lines (100%, 75%, 50%, 25%)
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("100%", "75%", "50%", "25%", "0%").forEach { pctLabel ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = pctLabel,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                modifier = Modifier.width(30.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                            )
                        }
                    }
                }

                // 6 Bars Row
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 32.dp, top = 8.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    trendList.forEach { trend ->
                        val isSelected = trend.monthItem.yearMonthKey == selectedMonthKey
                        val pct = trend.attendanceRatePct.coerceIn(0f, 100f)
                        val barHeightFactor = (pct / 100f).coerceAtLeast(0.08f)

                        val barColor = when {
                            pct >= 85f -> EmeraldSuccess
                            pct >= 70f -> AmberWarning
                            else -> RoseError
                        }

                        // Single Month Bar Column
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { onSelectMonth(trend.monthItem.yearMonthKey) }
                                .testTag("graph_bar_${trend.monthItem.yearMonthKey}")
                        ) {
                            // Badge with %
                            Text(
                                text = "${pct.toInt()}%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else barColor,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )

                            // Animated/Dynamic Height Bar Container
                            Box(
                                modifier = Modifier
                                    .width(28.dp)
                                    .fillMaxHeight(barHeightFactor)
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else barColor)
                                    .then(
                                        if (isSelected) Modifier.border(
                                            2.dp,
                                            Color.White,
                                            RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                        ) else Modifier
                                    )
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Month short label
                            Text(
                                text = trend.monthItem.displayName.take(3),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Legend Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(EmeraldSuccess, RoundedCornerShape(3.dp))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("High (≥85%)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(AmberWarning, RoundedCornerShape(3.dp))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Moderate (70-84%)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(RoseError, RoundedCornerShape(3.dp))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Low (<70%)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }
            }
        }
    }
}
