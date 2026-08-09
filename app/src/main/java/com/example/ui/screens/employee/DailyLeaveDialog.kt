package com.example.ui.screens.employee

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyLeaveDialog(
    onDismiss: () -> Unit,
    onSubmitLeave: (date: String, leaveType: String, reason: String) -> Unit
) {
    var leaveDate by remember { mutableStateOf(DateUtils.getTodayDateString()) }
    var leaveType by remember { mutableStateOf("Paid Leave") }
    var leaveReason by remember { mutableStateOf("") }
    var isLeaveTypeExpanded by remember { mutableStateOf(false) }

    val leaveTypes = listOf("Paid Leave", "Sick Leave", "Casual Leave", "Unpaid Leave")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("daily_leave_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EventBusy,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Request Daily Leave",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_leave_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = leaveDate,
                    onValueChange = { leaveDate = it },
                    label = { Text("Leave Date (YYYY-MM-DD)") },
                    placeholder = { Text("e.g. 2026-08-10") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("leave_date_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = isLeaveTypeExpanded,
                    onExpandedChange = { isLeaveTypeExpanded = !isLeaveTypeExpanded }
                ) {
                    OutlinedTextField(
                        value = leaveType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Leave Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isLeaveTypeExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("leave_type_dropdown"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = isLeaveTypeExpanded,
                        onDismissRequest = { isLeaveTypeExpanded = false }
                    ) {
                        leaveTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    leaveType = type
                                    isLeaveTypeExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = leaveReason,
                    onValueChange = { leaveReason = it },
                    label = { Text("Reason for Leave") },
                    placeholder = { Text("Provide details (e.g., Medical appointment, personal emergency...)") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("leave_reason_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (leaveReason.isNotBlank()) {
                                onSubmitLeave(leaveDate, leaveType, leaveReason)
                                onDismiss()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        enabled = leaveReason.isNotBlank(),
                        modifier = Modifier.testTag("submit_leave_request_btn")
                    ) {
                        Text("Submit Request")
                    }
                }
            }
        }
    }
}
