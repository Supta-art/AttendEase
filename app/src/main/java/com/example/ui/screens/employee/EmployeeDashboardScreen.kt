package com.example.ui.screens.employee

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AttendanceRecord
import com.example.data.model.Employee
import com.example.data.model.LeaveRequest
import com.example.ui.components.EmployeeAvatar
import com.example.ui.components.QrCodeImageView
import com.example.ui.theme.BentoDarkCard
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryContainer
import com.example.ui.theme.BentoSuccess
import com.example.ui.theme.BentoWarning
import com.example.ui.theme.RoseError
import com.example.util.DateUtils

@Composable
fun EmployeeDashboardScreen(
    employee: Employee?,
    todayRecord: AttendanceRecord?,
    historyLogs: List<AttendanceRecord>,
    myLeaveRequests: List<LeaveRequest>,
    onOpenQrScanner: () -> Unit,
    onCheckInNow: () -> Unit,
    onCheckOutNow: () -> Unit,
    onRequestLeaveClick: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    if (employee == null) return

    var showMyQrBadgeDialog by remember { mutableStateOf(false) }

    val remainingLeaveDays = (20 - myLeaveRequests.count { it.status == "APPROVED" }).coerceAtLeast(0)

    // Dynamic Attendance Rate calculation from history records
    val allLoggedRecords = remember(historyLogs, todayRecord) {
        val list = historyLogs.toMutableList()
        if (todayRecord != null && list.none { it.id == todayRecord.id || it.date == todayRecord.date }) {
            list.add(0, todayRecord)
        }
        list
    }

    val totalRecords = allLoggedRecords.size
    val onTimeOrPresentCount = allLoggedRecords.count { it.status == "PRESENT" || it.status == "LATE" }
    val calculatedAttendanceRate = if (totalRecords > 0) {
        ((onTimeOrPresentCount.toFloat() / totalRecords) * 100).toInt()
    } else {
        100
    }

    val weeklyFractions = remember(allLoggedRecords) {
        val recent7 = allLoggedRecords.take(7)
        if (recent7.isEmpty()) {
            listOf(1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.3f, 0.3f)
        } else {
            val result = mutableListOf<Float>()
            for (i in 0 until 7) {
                val rec = recent7.getOrNull(i)
                when (rec?.status) {
                    "PRESENT" -> result.add(1.0f)
                    "LATE" -> result.add(0.75f)
                    "ON_LEAVE" -> result.add(0.5f)
                    "ABSENT" -> result.add(0.2f)
                    else -> result.add(if (i >= 5) 0.3f else 0.85f)
                }
            }
            result
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("employee_dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Bento Card 1: Top Hero Scan Office QR Pass (Bento Accent Container)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = BentoPrimaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Daily QR Check-In Pass",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = DateUtils.getFormattedDate(DateUtils.getTodayDateString()),
                                style = MaterialTheme.typography.labelSmall,
                                color = BentoPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.8f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (todayRecord != null) "LOGGED (${todayRecord.status})" else "PENDING",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (todayRecord != null) BentoSuccess else BentoWarning
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Simulated dashed QR frame inside bento card
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(130.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(2.dp, BentoPrimary.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .clickable { showMyQrBadgeDialog = true }
                            .testTag("hero_qr_box")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = "My QR Code",
                                tint = BentoPrimary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "My QR Pass",
                                style = MaterialTheme.typography.labelSmall,
                                color = BentoPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showMyQrBadgeDialog = true },
                            modifier = Modifier.testTag("emp_view_qr_badge_btn")
                        ) {
                            Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("View My QR Pass", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        TextButton(
                            onClick = onOpenQrScanner,
                            modifier = Modifier.testTag("emp_open_scanner_btn")
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Scan QR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (todayRecord == null) {
                            Button(
                                onClick = onCheckInNow,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("emp_quick_checkin_btn"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Check-In Now", fontSize = 13.sp)
                            }
                        } else if (todayRecord.checkOutTime == null) {
                            Button(
                                onClick = onCheckOutNow,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("emp_quick_checkout_btn"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RoseError)
                            ) {
                                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Check-Out", fontSize = 13.sp)
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = BentoSuccess.copy(alpha = 0.15f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Completed check-out at ${DateUtils.getFormattedTime(todayRecord.checkOutTime)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BentoSuccess,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(12.dp),
                                    fontSize = 13.sp
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onRequestLeaveClick,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("emp_request_leave_btn")
                        ) {
                            Icon(Icons.Default.EventBusy, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Request Leave", fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Bento Row 2: Two Equal Bento Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Bento Card 2A: Time Off Remaining
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFEADDFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EventBusy,
                                    contentDescription = null,
                                    tint = Color(0xFF21005D),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "TIME OFF",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "$remainingLeaveDays",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Remaining Days",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                // Bento Card 2B: Dark Accent Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoDarkCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF33353D)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = null,
                                    tint = BentoPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "REPORTS",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )

                        Text(
                            text = "Personal Log Sync",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Bento Row 3: Weekly Streaks Bar Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
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
                        Text(
                            text = "WEEKLY STREAKS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )

                        Text(
                            text = "$calculatedAttendanceRate% Attendance Rate",
                            style = MaterialTheme.typography.labelMedium,
                            color = BentoPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 7-day pill bars
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        weeklyFractions.forEach { fraction ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height((48 * fraction).dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (fraction > 0.5f) BentoPrimaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                            Text(
                                text = day,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }

        // Bento Row 4: Recent History Logs Card
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Attendance History Logs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "View All",
                    style = MaterialTheme.typography.labelLarge,
                    color = BentoPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onNavigateToHistory() }
                        .testTag("emp_view_all_history")
                        .padding(4.dp)
                )
            }
        }

        if (historyLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No attendance history logs recorded yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        } else {
            items(historyLogs.take(4)) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val dotColor = when (log.status) {
                                "PRESENT" -> BentoSuccess
                                "LATE" -> BentoWarning
                                "ON_LEAVE" -> BentoPrimary
                                else -> RoseError
                            }

                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = when (log.status) {
                                        "PRESENT" -> "Checked In On Time"
                                        "LATE" -> "Late Arrival"
                                        "ON_LEAVE" -> "On Leave"
                                        else -> "Absent"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "${DateUtils.getFormattedDate(log.date)} • ${DateUtils.getFormattedTime(log.checkInTime)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = log.method,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog showing the active user's personal QR Code Pass
    if (showMyQrBadgeDialog) {
        Dialog(onDismissRequest = { showMyQrBadgeDialog = false }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("my_qr_badge_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val dynamicToken = "${employee.qrToken}-${System.currentTimeMillis() / 60000L}"

                    QrCodeImageView(
                        employeeName = employee.name,
                        employeeId = employee.employeeId,
                        qrToken = dynamicToken
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = { showMyQrBadgeDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close Pass", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
