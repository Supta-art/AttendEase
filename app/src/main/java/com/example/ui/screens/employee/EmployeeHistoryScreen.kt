package com.example.ui.screens.employee

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
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
import com.example.data.model.AttendanceRecord
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoSuccess
import com.example.ui.theme.BentoWarning
import com.example.ui.theme.RoseError
import com.example.util.DateUtils

@Composable
fun EmployeeHistoryScreen(
    attendanceLogs: List<AttendanceRecord>
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterStatus by remember { mutableStateOf("ALL") } // ALL, PRESENT, LATE, ON_LEAVE

    val filteredLogs = remember(attendanceLogs, searchQuery, selectedFilterStatus) {
        attendanceLogs.filter { log ->
            val matchesQuery = searchQuery.isBlank() ||
                    log.date.contains(searchQuery) ||
                    log.method.contains(searchQuery, ignoreCase = true)
            val matchesStatus = selectedFilterStatus == "ALL" || log.status == selectedFilterStatus
            matchesQuery && matchesStatus
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("employee_history_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = BentoPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Historical Attendance Logs",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Filter logs by date or method...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("history_search_input"),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(listOf("ALL", "PRESENT", "LATE", "ON_LEAVE")) { status ->
                            FilterChip(
                                selected = selectedFilterStatus == status,
                                onClick = { selectedFilterStatus = status },
                                label = {
                                    Text(
                                        text = when (status) {
                                            "ALL" -> "All Logs (${attendanceLogs.size})"
                                            "PRESENT" -> "Present"
                                            "LATE" -> "Late"
                                            "ON_LEAVE" -> "On Leave"
                                            else -> status
                                        },
                                        fontSize = 12.sp
                                    )
                                },
                                modifier = Modifier.testTag("filter_chip_$status")
                            )
                        }
                    }
                }
            }
        }

        if (filteredLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No matching attendance records found.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        } else {
            items(filteredLogs) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val (statusText, statusBg, statusColor) = when (log.status) {
                                    "PRESENT" -> Triple("PRESENT", BentoSuccess.copy(alpha = 0.15f), BentoSuccess)
                                    "LATE" -> Triple("LATE", BentoWarning.copy(alpha = 0.15f), BentoWarning)
                                    "ON_LEAVE" -> Triple("ON LEAVE", BentoPrimary.copy(alpha = 0.15f), BentoPrimary)
                                    else -> Triple("ABSENT", RoseError.copy(alpha = 0.15f), RoseError)
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(statusBg)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = statusText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = statusColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Text(
                                    text = DateUtils.getFormattedDate(log.date),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "Via ${log.method}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Check-In: ${DateUtils.getFormattedTime(log.checkInTime)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )

                            if (log.checkOutTime != null) {
                                Text(
                                    text = "Check-Out: ${DateUtils.getFormattedTime(log.checkOutTime)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
