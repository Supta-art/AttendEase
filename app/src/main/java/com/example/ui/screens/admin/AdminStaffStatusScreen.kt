package com.example.ui.screens.admin

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AttendanceRecord
import com.example.data.model.Employee
import com.example.ui.components.EmployeeAvatar
import com.example.ui.components.StatCard
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError
import com.example.util.DateUtils

@Composable
fun AdminStaffStatusScreen(
    employees: List<Employee>,
    todayRecords: List<AttendanceRecord>,
    onDeleteEmployee: (String) -> Unit,
    onSelectEmployeeForPortal: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterStatus by remember { mutableStateOf("ALL") } // ALL, PRESENT, LATE, NOT_CHECKED_IN
    var empToDeleteConfirm by remember { mutableStateOf<Employee?>(null) }

    // Map employee ID -> today record
    val todayRecordMap = remember(todayRecords) {
        todayRecords.associateBy { it.employeeId }
    }

    // Calculations for check-in status summary
    val totalEmployees = employees.size
    val presentCount = todayRecords.count { it.status == "PRESENT" }
    val lateCount = todayRecords.count { it.status == "LATE" }
    val leaveCount = todayRecords.count { it.status == "ON_LEAVE" }
    val checkedInTotal = presentCount + lateCount
    val notCheckedInCount = (totalEmployees - checkedInTotal - leaveCount).coerceAtLeast(0)

    val checkInPct = if (totalEmployees > 0) (checkedInTotal.toFloat() / totalEmployees * 100).toInt() else 0

    val filteredEmployees = remember(employees, searchQuery, selectedFilterStatus, todayRecordMap) {
        employees.filter { emp ->
            val matchesQuery = searchQuery.isBlank() ||
                    emp.name.contains(searchQuery, ignoreCase = true) ||
                    emp.department.contains(searchQuery, ignoreCase = true) ||
                    emp.role.contains(searchQuery, ignoreCase = true) ||
                    emp.employeeId.contains(searchQuery, ignoreCase = true)

            val rec = todayRecordMap[emp.employeeId]
            val matchesFilter = when (selectedFilterStatus) {
                "PRESENT" -> rec?.status == "PRESENT"
                "LATE" -> rec?.status == "LATE"
                "NOT_CHECKED_IN" -> rec == null || rec.status == "NOT_CHECKED_IN"
                else -> true
            }

            matchesQuery && matchesFilter
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_staff_status_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Column {
                Text(
                    text = "Staff Check-In Status & Roster",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Real-time summary of today's attendance and staff records",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        // Summary Statistics Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        StatCard(
                            title = "Total Staff",
                            value = "$totalEmployees",
                            icon = Icons.Default.Group,
                            iconBgColor = BentoPrimary.copy(alpha = 0.15f),
                            iconTintColor = BentoPrimary
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        StatCard(
                            title = "Checked In",
                            value = "$checkedInTotal ($checkInPct%)",
                            icon = Icons.Default.CheckCircle,
                            iconBgColor = EmeraldSuccess.copy(alpha = 0.15f),
                            iconTintColor = EmeraldSuccess
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        StatCard(
                            title = "Arrived Late",
                            value = "$lateCount",
                            icon = Icons.Default.MoreTime,
                            iconBgColor = AmberWarning.copy(alpha = 0.15f),
                            iconTintColor = AmberWarning
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        StatCard(
                            title = "Pending Check-In",
                            value = "$notCheckedInCount",
                            icon = Icons.Default.PersonOff,
                            iconBgColor = RoseError.copy(alpha = 0.15f),
                            iconTintColor = RoseError
                        )
                    }
                }
            }
        }

        // Visual Progress & Breakdown Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("check_in_summary_banner"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
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
                            text = "Today's Attendance Rate",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "$checkInPct%",
                            fontWeight = FontWeight.ExtraBold,
                            color = if (checkInPct >= 80) EmeraldSuccess else AmberWarning,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { (checkInPct / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = if (checkInPct >= 80) EmeraldSuccess else AmberWarning,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🟢 On-Time: $presentCount",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "🟠 Late: $lateCount",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "🔴 Pending: $notCheckedInCount",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Search & Filter Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("staff_status_search_input"),
                    placeholder = { Text("Search by name, role, department or ID...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        FilterChip(
                            selected = selectedFilterStatus == "ALL",
                            onClick = { selectedFilterStatus = "ALL" },
                            label = { Text("All Staff ($totalEmployees)") },
                            modifier = Modifier.testTag("filter_chip_all")
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilterStatus == "PRESENT",
                            onClick = { selectedFilterStatus = "PRESENT" },
                            label = { Text("On Time ($presentCount)") },
                            modifier = Modifier.testTag("filter_chip_present")
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilterStatus == "LATE",
                            onClick = { selectedFilterStatus = "LATE" },
                            label = { Text("Late ($lateCount)") },
                            modifier = Modifier.testTag("filter_chip_late")
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilterStatus == "NOT_CHECKED_IN",
                            onClick = { selectedFilterStatus = "NOT_CHECKED_IN" },
                            label = { Text("Not Checked In ($notCheckedInCount)") },
                            modifier = Modifier.testTag("filter_chip_not_checked_in")
                        )
                    }
                }
            }
        }

        // Employee Roster Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Registered Staff Directory (${filteredEmployees.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Employees List items
        if (filteredEmployees.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No staff members matching criteria found.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        } else {
            items(filteredEmployees, key = { it.employeeId }) { emp ->
                val record = todayRecordMap[emp.employeeId]

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("staff_card_${emp.employeeId}"),
                    shape = RoundedCornerShape(18.dp),
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
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            EmployeeAvatar(
                                name = emp.name,
                                photoUri = emp.photoUri,
                                size = 48.dp
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = emp.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "${emp.role} • ${emp.department}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "ID: ${emp.employeeId}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BentoPrimary
                                )
                            }

                            // Delete Action Button
                            IconButton(
                                onClick = { empToDeleteConfirm = emp },
                                modifier = Modifier.testTag("delete_staff_${emp.employeeId}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Staff",
                                    tint = RoseError
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Status Badge Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val (badgeBg, badgeFg, labelText) = when {
                                record?.status == "PRESENT" -> Triple(
                                    EmeraldSuccess.copy(alpha = 0.15f),
                                    EmeraldSuccess,
                                    "✓ Checked In (${DateUtils.getFormattedTime(record.checkInTime)})"
                                )
                                record?.status == "LATE" -> Triple(
                                    AmberWarning.copy(alpha = 0.15f),
                                    AmberWarning,
                                    "⚠️ Arrived Late (${DateUtils.getFormattedTime(record.checkInTime)})"
                                )
                                record?.status == "ON_LEAVE" -> Triple(
                                    BentoPrimary.copy(alpha = 0.15f),
                                    BentoPrimary,
                                    "✈️ On Approved Leave"
                                )
                                else -> Triple(
                                    RoseError.copy(alpha = 0.12f),
                                    RoseError,
                                    "⏳ Not Checked In Today"
                                )
                            }

                            Surface(
                                color = badgeBg,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = labelText,
                                    color = badgeFg,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }

                            TextButton(
                                onClick = { onSelectEmployeeForPortal(emp.employeeId) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("View Portal", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog for Staff Deletion
    val empToDelete = empToDeleteConfirm
    if (empToDelete != null) {
        Dialog(onDismissRequest = { empToDeleteConfirm = null }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("delete_staff_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(RoseError.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = RoseError,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Delete Staff Member?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = RoseError
                            )
                            Text(
                                text = "Action cannot be undone",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Are you sure you want to permanently remove \"${empToDelete.name}\" (ID: ${empToDelete.employeeId}) from the system? Their registered profile and associated records will be deleted.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { empToDeleteConfirm = null }) {
                            Text("Cancel")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                onDeleteEmployee(empToDelete.employeeId)
                                empToDeleteConfirm = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RoseError),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("confirm_delete_staff_btn")
                        ) {
                            Text("Delete Permanently")
                        }
                    }
                }
            }
        }
    }
}
