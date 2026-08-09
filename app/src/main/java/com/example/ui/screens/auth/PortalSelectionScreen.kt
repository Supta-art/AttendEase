package com.example.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Employee
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryContainer
import com.example.ui.theme.BentoSuccess
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.IndigoSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortalSelectionScreen(
    employees: List<Employee>,
    selectedEmployee: Employee?,
    onSelectAdminPortal: () -> Unit,
    onSelectEmployeePortal: (employeeId: String) -> Unit,
    onLoginWithEmailPass: (email: String, pass: String, role: String, onResult: (Boolean, String) -> Unit) -> Unit = { _, _, _, _ -> },
    onRegisterDetails: (empId: String, name: String, email: String, pass: String, dept: String, desig: String, phone: String, role: String, photoUri: String?, onResult: (Boolean, String) -> Unit) -> Unit = { _, _, _, _, _, _, _, _, _, _ -> },
    onAutoFillFromDrive: (identifier: String, onResult: (Employee?) -> Unit) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    // -1 = Role Selection Screen ("Are you Admin or Staff?"), 0 = Staff Portal, 1 = Admin Portal
    var selectedRoleTab by remember { mutableStateOf(-1) }
    var isRegisterMode by remember { mutableStateOf(false) }

    // Login Fields
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Setup / Fill Details Fields
    var detailEmpId by remember { mutableStateOf("") }
    var detailName by remember { mutableStateOf("") }
    var detailEmail by remember { mutableStateOf("") }
    var detailPassword by remember { mutableStateOf("") }
    var detailDepartment by remember { mutableStateOf("") }
    var detailDesignation by remember { mutableStateOf("") }
    var detailPhone by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
            .testTag("portal_selection_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Branding Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(IndigoPrimary, IndigoSecondary)
                            )
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = "AttendEase",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "AttendEase",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 26.sp
                )

                Text(
                    text = "Attendance & Identity Portal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Error message box
        if (errorMessage != null) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // STEP 1: INITIAL ROLE CHOICE ("Are you an Admin or a Staff?")
        if (selectedRoleTab == -1) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Are you an Admin or a Staff Member?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    // Option 1: Admin Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedRoleTab = 1
                                errorMessage = null
                                emailInput = ""
                                passwordInput = ""
                                isRegisterMode = false
                            }
                            .testTag("select_admin_role_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = BentoPrimaryContainer.copy(alpha = 0.35f)),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(BentoPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = "Admin",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "I am an Admin",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Access organization management, attendance logs & approvals.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = BentoPrimary
                            )
                        }
                    }

                    // Option 2: Staff / Employee Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedRoleTab = 0
                                errorMessage = null
                                emailInput = ""
                                passwordInput = ""
                                isRegisterMode = false
                            }
                            .testTag("select_staff_role_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(BentoSuccess),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Staff Member",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "I am a Staff Member",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Mark check-in, apply for leave & auto-fill from Google Drive.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = BentoSuccess
                            )
                        }
                    }
                }
            }
        } else {
            // STEP 2: LOGIN / REGISTRATION FORM FOR THE SELECTED ROLE (Admin or Staff)
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Top navigation bar to switch role / go back to role selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                selectedRoleTab = -1
                                errorMessage = null
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Change Role", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedRoleTab == 1) BentoPrimaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (selectedRoleTab == 1) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (selectedRoleTab == 1) BentoPrimary else MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (selectedRoleTab == 1) "Admin Portal" else "Staff Portal",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedRoleTab == 1) BentoPrimary else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Authentication & Details Form Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isRegisterMode) "Fill Details & Register" else if (selectedRoleTab == 1) "Admin Login" else "Staff Login",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                TextButton(
                                    onClick = {
                                        isRegisterMode = !isRegisterMode
                                        errorMessage = null
                                    }
                                ) {
                                    Text(
                                        text = if (isRegisterMode) "Switch to Login" else "First Time? Register",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (!isRegisterMode) {
                                // Google Drive Cloud Sync Active Banner
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = BentoPrimaryContainer.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudDone,
                                            contentDescription = null,
                                            tint = BentoPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Google Drive Sync • Auto-fills saved profile details",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoPrimary
                                        )
                                    }
                                }

                                // Name or Email Field
                                OutlinedTextField(
                                    value = emailInput,
                                    onValueChange = { emailInput = it },
                                    label = { Text("Full Name or Email Address") },
                                    placeholder = { Text("e.g. Alex Rivera or alex.rivera@attendease.com") },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                    trailingIcon = {
                                        if (emailInput.isNotBlank()) {
                                            IconButton(
                                                onClick = {
                                                    onAutoFillFromDrive(emailInput) { restored ->
                                                        if (restored != null) {
                                                            emailInput = restored.email
                                                            passwordInput = restored.password
                                                        }
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CloudDownload,
                                                    contentDescription = "Restore from Drive",
                                                    tint = BentoPrimary
                                                )
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("login_email_input"),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                // Password Field
                                OutlinedTextField(
                                    value = passwordInput,
                                    onValueChange = { passwordInput = it },
                                    label = { Text("Password") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                    trailingIcon = {
                                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                            Icon(
                                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = null
                                            )
                                        }
                                    },
                                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("login_password_input"),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                // Quick Auto-Fill from Google Drive button
                                OutlinedButton(
                                    onClick = {
                                        onAutoFillFromDrive(emailInput) { restored ->
                                            if (restored != null) {
                                                emailInput = restored.email
                                                passwordInput = restored.password
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Auto-fill Details from Google Drive ☁️", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        val role = if (selectedRoleTab == 1) "ADMIN" else "EMPLOYEE"
                                        onLoginWithEmailPass(emailInput, passwordInput, role) { success, msg ->
                                            if (!success) {
                                                errorMessage = msg
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("submit_login_btn"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selectedRoleTab == 1) BentoPrimary else BentoSuccess
                                    )
                                ) {
                                    Text(
                                        text = if (selectedRoleTab == 1) "Log in as Admin" else "Log in as Staff",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                                }
                            } else {
                                // Registration / Detail filling Mode
                                Text(
                                    text = if (selectedRoleTab == 1) "Fill details to create Admin account:" else "Fill details to create Staff account:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )

                                // Individual ID
                                OutlinedTextField(
                                    value = detailEmpId,
                                    onValueChange = { detailEmpId = it },
                                    label = { Text("Staff / Admin ID (e.g. EMP-1008)") },
                                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                // Full Name
                                OutlinedTextField(
                                    value = detailName,
                                    onValueChange = { detailName = it },
                                    label = { Text("Full Name") },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                // Email
                                OutlinedTextField(
                                    value = detailEmail,
                                    onValueChange = { detailEmail = it },
                                    label = { Text("Email Address") },
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                // Password
                                OutlinedTextField(
                                    value = detailPassword,
                                    onValueChange = { detailPassword = it },
                                    label = { Text("Password") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                // Department
                                OutlinedTextField(
                                    value = detailDepartment,
                                    onValueChange = { detailDepartment = it },
                                    label = { Text("Department") },
                                    leadingIcon = { Icon(Icons.Default.Work, contentDescription = null) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                // Job Designation
                                OutlinedTextField(
                                    value = detailDesignation,
                                    onValueChange = { detailDesignation = it },
                                    label = { Text("Designation / Title") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                // Phone
                                OutlinedTextField(
                                    value = detailPhone,
                                    onValueChange = { detailPhone = it },
                                    label = { Text("Phone Number") },
                                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Button(
                                    onClick = {
                                        val role = if (selectedRoleTab == 1) "ADMIN" else "EMPLOYEE"
                                        onRegisterDetails(
                                            detailEmpId,
                                            detailName,
                                            detailEmail,
                                            detailPassword,
                                            detailDepartment,
                                            detailDesignation,
                                            detailPhone,
                                            role,
                                            null
                                        ) { success, msg ->
                                            if (!success) {
                                                errorMessage = msg
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("submit_register_btn"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
                                ) {
                                    Text(
                                        text = "Save Details & Continue",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
