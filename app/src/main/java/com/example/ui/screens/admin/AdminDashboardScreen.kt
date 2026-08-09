package com.example.ui.screens.admin

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AttendanceRecord
import com.example.data.model.Employee
import com.example.data.model.LeaveRequest
import com.example.ui.components.EmployeeAvatar
import com.example.ui.components.StatCard
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError
import com.example.util.DateUtils

@Composable
fun AdminDashboardScreen(
    employees: List<Employee>,
    todayRecords: List<AttendanceRecord>,
    pendingLeaves: List<LeaveRequest>,
    onOpenScanner: () -> Unit,
    onNavigateToLeaves: () -> Unit,
    onNavigateToReports: () -> Unit,
    onApproveLeave: (Long, String, String) -> Unit,
    onRejectLeave: (Long, String, String) -> Unit
) {
    val totalEmployees = employees.size.coerceAtLeast(1)
    val presentCount = todayRecords.count { it.status == "PRESENT" }
    val lateCount = todayRecords.count { it.status == "LATE" }
    val leaveCount = todayRecords.count { it.status == "ON_LEAVE" }
    val absentCount = (totalEmployees - (presentCount + lateCount + leaveCount)).coerceAtLeast(0)
    val attendancePct = (((presentCount + lateCount).toFloat() / totalEmployees) * 100f).toInt()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Banner Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Real-Time Attendance Overview",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Today's check-ins: $presentCount Present, $lateCount Late, $leaveCount On Leave",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onOpenScanner,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("admin_scan_qr_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Scan Daily QR", fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = onNavigateToReports,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("view_6m_reports_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EventAvailable,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("6-Month Reports", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // 4 KPI Grid Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Present Today",
                        value = "$presentCount / $totalEmployees",
                        icon = Icons.Default.CheckCircle,
                        iconBgColor = EmeraldSuccess.copy(alpha = 0.15f),
                        iconTintColor = EmeraldSuccess,
                        subtitle = "$attendancePct%",
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        title = "Late Arrivals",
                        value = "$lateCount",
                        icon = Icons.Default.MoreTime,
                        iconBgColor = AmberWarning.copy(alpha = 0.15f),
                        iconTintColor = AmberWarning,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "On Leave Today",
                        value = "$leaveCount",
                        icon = Icons.Default.EventBusy,
                        iconBgColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        iconTintColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        title = "Total Employees",
                        value = "$totalEmployees",
                        icon = Icons.Default.Group,
                        iconBgColor = Color(0xFF6366F1).copy(alpha = 0.15f),
                        iconTintColor = Color(0xFF6366F1),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Pending Leave Requests Quick Approval Section
        if (pendingLeaves.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pending Leave Requests (${pendingLeaves.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Manage All",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.testTag("admin_view_all_leaves").padding(4.dp)
                    )
                }
            }

            items(pendingLeaves.take(3)) { leave ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
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
                                    name = leave.employeeName,
                                    photoUri = null,
                                    size = 36.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = leave.employeeName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${leave.department} • ${leave.leaveType}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AmberWarning.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = leave.date,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AmberWarning,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Reason: ${leave.reason}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { onRejectLeave(leave.id, leave.employeeId, leave.date) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("reject_leave_btn_${leave.id}")
                            ) {
                                Text("Reject", color = RoseError, fontSize = 12.sp)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = { onApproveLeave(leave.id, leave.employeeId, leave.date) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("approve_leave_btn_${leave.id}")
                            ) {
                                Text("Approve", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Live Today Check-ins List
        item {
            Text(
                text = "Today's Live Check-In Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (todayRecords.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No employee check-in logs recorded yet today.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        } else {
            items(todayRecords) { record ->
                val emp = employees.find { it.employeeId == record.employeeId }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            EmployeeAvatar(
                                name = emp?.name ?: record.employeeId,
                                photoUri = emp?.photoUri,
                                size = 40.dp
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = emp?.name ?: record.employeeId,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Checked In: ${DateUtils.getFormattedTime(record.checkInTime)} via ${record.method}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }

                        val (statusText, badgeBg, badgeText) = when (record.status) {
                            "PRESENT" -> Triple("PRESENT", EmeraldSuccess.copy(alpha = 0.15f), EmeraldSuccess)
                            "LATE" -> Triple("LATE", AmberWarning.copy(alpha = 0.15f), AmberWarning)
                            "ON_LEAVE" -> Triple("ON LEAVE", MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), MaterialTheme.colorScheme.primary)
                            else -> Triple("ABSENT", RoseError.copy(alpha = 0.15f), RoseError)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(badgeBg)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelSmall,
                                color = badgeText,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
