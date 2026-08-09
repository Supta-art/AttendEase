package com.example.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Employee
import com.example.ui.components.EmployeeAvatar
import com.example.ui.components.QrCodeImageView
import kotlinx.coroutines.delay

@Composable
fun AdminQrManagerScreen(
    employees: List<Employee>,
    onOpenScanner: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedEmployee by remember(employees) {
        mutableStateOf(employees.firstOrNull())
    }

    // 1-Minute Auto Refresh Timer State (60 Seconds)
    var secondsLeft by remember { mutableIntStateOf(60) }
    var refreshBatchTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            if (secondsLeft > 1) {
                secondsLeft -= 1
            } else {
                secondsLeft = 60
                refreshBatchTimestamp = System.currentTimeMillis()
            }
        }
    }

    val filteredEmployees = remember(employees, searchQuery) {
        if (searchQuery.isBlank()) employees
        else employees.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.employeeId.contains(searchQuery, ignoreCase = true) ||
            it.department.contains(searchQuery, ignoreCase = true)
        }
    }

    val activeSelectedEmployee = remember(filteredEmployees, selectedEmployee) {
        if (selectedEmployee != null && filteredEmployees.any { it.employeeId == selectedEmployee?.employeeId }) {
            selectedEmployee
        } else {
            filteredEmployees.firstOrNull()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_qr_manager_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Live QR Pass Generator",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Auto-refreshes every 1 minute for anti-spoof security",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = onOpenScanner,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("admin_launch_scanner_btn")
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Scan QR", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1-minute countdown timer banner
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Autorenew,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Refreshes in $secondsLeft seconds",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                secondsLeft = 60
                                refreshBatchTimestamp = System.currentTimeMillis()
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Refresh Now", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { secondsLeft / 60f },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search employee by name, ID, or department...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_qr_search_input"),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )
                }
            }
        }

        // Employee Horizontal Selection Chips (Only shown when multiple employees exist)
        if (filteredEmployees.size > 1) {
            item {
                Column {
                    Text(
                        text = "Select Staff Member:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredEmployees) { emp ->
                            val isSelected = activeSelectedEmployee?.employeeId == emp.employeeId
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedEmployee = emp },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        EmployeeAvatar(name = emp.name, photoUri = emp.photoUri, size = 22.dp, showVerifiedBadge = false)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(emp.name, fontSize = 12.sp)
                                    }
                                },
                                modifier = Modifier.testTag("emp_chip_${emp.employeeId}")
                            )
                        }
                    }
                }
            }
        }

        // Selected Employee QR Display Card
        item {
            val emp = activeSelectedEmployee
            if (emp != null) {
                // Generate token appended with the current 1-minute batch interval
                val dynamicToken = "${emp.qrToken}-${refreshBatchTimestamp / 60000L}"

                QrCodeImageView(
                    employeeName = emp.name,
                    employeeId = emp.employeeId,
                    qrToken = dynamicToken
                )
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No employee selected.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}
