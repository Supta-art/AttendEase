package com.example.ui.screens.admin

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Employee
import com.example.ui.components.EmployeeAvatar
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.RoseError
import com.example.util.DateUtils
import kotlin.random.Random

@Composable
fun AdminEmployeesScreen(
    employees: List<Employee>,
    onSaveEmployee: (Employee) -> Unit,
    onDeleteEmployee: (String) -> Unit,
    onSelectEmployeeForPortal: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var isAddEmployeeDialogOpen by remember { mutableStateOf(false) }
    var selectedEmpForEdit by remember { mutableStateOf<Employee?>(null) }
    var empToDeleteConfirm by remember { mutableStateOf<Employee?>(null) }

    val filteredEmployees = remember(employees, searchQuery) {
        if (searchQuery.isBlank()) employees
        else employees.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.employeeId.contains(searchQuery, ignoreCase = true) ||
            it.department.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_employees_screen"),
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = BentoPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Employee Directory (${employees.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { isAddEmployeeDialogOpen = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("admin_add_employee_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Staff", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by staff name, ID, or department...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_emp_search_input"),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )
                }
            }
        }

        items(filteredEmployees) { emp ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("employee_card_${emp.employeeId}"),
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            EmployeeAvatar(
                                name = emp.name,
                                photoUri = emp.photoUri,
                                size = 48.dp,
                                showVerifiedBadge = true
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = emp.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "${emp.department} • ID: ${emp.employeeId}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )

                                Text(
                                    text = emp.email,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BentoPrimary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { selectedEmpForEdit = emp },
                                modifier = Modifier.testTag("edit_emp_${emp.employeeId}")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = BentoPrimary)
                            }

                            IconButton(
                                onClick = { empToDeleteConfirm = emp },
                                modifier = Modifier.testTag("delete_emp_${emp.employeeId}")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Staff", tint = RoseError)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { onSelectEmployeeForPortal(emp.employeeId) },
                            modifier = Modifier.testTag("switch_to_emp_portal_${emp.employeeId}")
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Switch to Portal View", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    if (isAddEmployeeDialogOpen) {
        AddEmployeeDialog(
            onDismiss = { isAddEmployeeDialogOpen = false },
            onSave = { newEmp ->
                onSaveEmployee(newEmp)
                isAddEmployeeDialogOpen = false
            }
        )
    }

    val empToEdit = selectedEmpForEdit
    if (empToEdit != null) {
        AddEmployeeDialog(
            existingEmployee = empToEdit,
            onDismiss = { selectedEmpForEdit = null },
            onSave = { updatedEmp ->
                onSaveEmployee(updatedEmp)
                selectedEmpForEdit = null
            }
        )
    }

    val empToDelete = empToDeleteConfirm
    if (empToDelete != null) {
        Dialog(onDismissRequest = { empToDeleteConfirm = null }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("delete_employee_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = RoseError,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Delete Staff Member?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RoseError
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Are you sure you want to permanently delete staff member \"${empToDelete.name}\" (ID: ${empToDelete.employeeId})? This authority action will remove them and their records from the system.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

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
                            modifier = Modifier.testTag("confirm_delete_emp_btn")
                        ) {
                            Text("Delete Permanently")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddEmployeeDialog(
    existingEmployee: Employee? = null,
    onDismiss: () -> Unit,
    onSave: (Employee) -> Unit
) {
    var name by remember { mutableStateOf(existingEmployee?.name ?: "") }
    var department by remember { mutableStateOf(existingEmployee?.department ?: "Engineering") }
    var email by remember { mutableStateOf(existingEmployee?.email ?: "") }
    var role by remember { mutableStateOf(existingEmployee?.role ?: "EMPLOYEE") }
    var photoUri by remember { mutableStateOf(existingEmployee?.photoUri ?: "") }

    val mockAvatars = listOf(
        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
        "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
        "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
        "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_employee_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = if (existingEmployee == null) "Add New Staff Member" else "Edit Staff Member",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Verification Photo Avatar:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    mockAvatars.forEachIndexed { idx, url ->
                        Box(
                            modifier = Modifier
                                .clickable { photoUri = url }
                                .testTag("select_new_avatar_$idx")
                        ) {
                            EmployeeAvatar(
                                name = name.ifBlank { "Staff" },
                                photoUri = url,
                                size = 40.dp,
                                showVerifiedBadge = photoUri == url
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_emp_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("Department") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_emp_dept_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Corporate Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_emp_email_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                val empId = existingEmployee?.employeeId ?: "EMP-${Random.nextInt(1000, 9999)}"
                                val qrToken = existingEmployee?.qrToken ?: "ATT-$empId-${Random.nextInt(10000, 99999)}"
                                val emp = Employee(
                                    employeeId = empId,
                                    name = name,
                                    email = if (email.isBlank()) "${name.lowercase().replace(" ", ".")}@company.com" else email,
                                    phone = existingEmployee?.phone ?: "+1 555-0199",
                                    role = role,
                                    department = department,
                                    designation = existingEmployee?.designation ?: "Staff Specialist",
                                    photoUri = photoUri.ifBlank { mockAvatars.first() },
                                    qrToken = qrToken,
                                    dateJoined = existingEmployee?.dateJoined ?: System.currentTimeMillis()
                                )
                                onSave(emp)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        enabled = name.isNotBlank(),
                        modifier = Modifier.testTag("save_add_emp_btn")
                    ) {
                        Text("Save Staff")
                    }
                }
            }
        }
    }
}
