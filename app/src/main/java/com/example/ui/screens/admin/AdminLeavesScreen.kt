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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventBusy
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
import com.example.data.model.LeaveRequest
import com.example.ui.components.EmployeeAvatar
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoSuccess
import com.example.ui.theme.BentoWarning
import com.example.ui.theme.RoseError

@Composable
fun AdminLeavesScreen(
    leaveRequests: List<LeaveRequest>,
    onApproveLeave: (Long, String, String) -> Unit,
    onRejectLeave: (Long, String, String) -> Unit
) {
    var selectedFilterStatus by remember { mutableStateOf("PENDING") } // PENDING, APPROVED, REJECTED, ALL

    val filteredLeaves = remember(leaveRequests, selectedFilterStatus) {
        if (selectedFilterStatus == "ALL") leaveRequests
        else leaveRequests.filter { it.status == selectedFilterStatus }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_leaves_screen"),
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
                            imageVector = Icons.Default.EventBusy,
                            contentDescription = null,
                            tint = BentoPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Leave Request Approvals",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(listOf("PENDING", "APPROVED", "REJECTED", "ALL")) { status ->
                            FilterChip(
                                selected = selectedFilterStatus == status,
                                onClick = { selectedFilterStatus = status },
                                label = {
                                    val count = if (status == "ALL") leaveRequests.size else leaveRequests.count { it.status == status }
                                    Text("$status ($count)", fontSize = 12.sp)
                                },
                                modifier = Modifier.testTag("leave_filter_$status")
                            )
                        }
                    }
                }
            }
        }

        if (filteredLeaves.isEmpty()) {
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
                            text = "No leave requests found for status: $selectedFilterStatus",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        } else {
            items(filteredLeaves) { leave ->
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
                                EmployeeAvatar(
                                    name = leave.employeeName,
                                    photoUri = null,
                                    size = 40.dp
                                )

                                Spacer(modifier = Modifier.width(12.dp))

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

                            val (bg, txtColor) = when (leave.status) {
                                "APPROVED" -> Pair(BentoSuccess.copy(alpha = 0.15f), BentoSuccess)
                                "REJECTED" -> Pair(RoseError.copy(alpha = 0.15f), RoseError)
                                else -> Pair(BentoWarning.copy(alpha = 0.15f), BentoWarning)
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bg)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = leave.status,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = txtColor,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Date Requested: ${leave.date}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = BentoPrimary
                        )

                        Text(
                            text = "Reason: ${leave.reason}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        if (leave.status == "PENDING") {
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = { onRejectLeave(leave.id, leave.employeeId, leave.date) },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("reject_leave_item_${leave.id}")
                                ) {
                                    Text("Reject", color = RoseError, fontSize = 12.sp)
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = { onApproveLeave(leave.id, leave.employeeId, leave.date) },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("approve_leave_item_${leave.id}")
                                ) {
                                    Text("Approve", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
