package com.example.ui.screens.employee

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.example.util.DateUtils
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Work
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Employee
import com.example.ui.components.EmployeeAvatar
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryContainer
import com.example.util.GoogleDriveSyncManager

@Composable
fun EmployeeProfileScreen(
    employee: Employee?,
    onSaveProfile: (Employee) -> Unit
) {
    if (employee == null) return

    val context = LocalContext.current
    var isEditingProfile by remember { mutableStateOf(false) }
    var syncStatusText by remember { mutableStateOf(GoogleDriveSyncManager.getLastSyncTimestamp(context)) }

    // Photo picker launcher for Google Photos / Gallery
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val updated = employee.copy(photoUri = it.toString())
            onSaveProfile(updated)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("employee_profile_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Profile Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        EmployeeAvatar(
                            name = employee.name,
                            photoUri = employee.photoUri,
                            size = 88.dp,
                            showVerifiedBadge = true
                        )

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(BentoPrimary)
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .testTag("change_photo_badge")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Choose photo",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = employee.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${employee.role} • ${employee.department}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("pick_photo_btn")
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Choose Photo", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { isEditingProfile = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("edit_profile_btn")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit Profile", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Details Grid (NO QR CODE IS DISPLAYED HERE - Employees scan QR each time to check in/out)
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
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Verified Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    ProfileDetailRow(
                        icon = Icons.Default.Badge,
                        label = "Individual Employee ID",
                        value = employee.employeeId
                    )

                    ProfileDetailRow(
                        icon = Icons.Default.Email,
                        label = "Corporate Email",
                        value = employee.email
                    )

                    ProfileDetailRow(
                        icon = Icons.Default.Work,
                        label = "Department & Designation",
                        value = "${employee.department} (${employee.designation})"
                    )

                    ProfileDetailRow(
                        icon = Icons.Default.Phone,
                        label = "Contact Phone",
                        value = employee.phone.ifBlank { "Not provided" }
                    )

                    ProfileDetailRow(
                        icon = Icons.Default.Person,
                        label = "Account Status",
                        value = "Verified Active Staff"
                    )
                }
            }
        }

        // Google Drive Cloud Sync Status Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = BentoPrimaryContainer.copy(alpha = 0.35f)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(BentoPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = "Google Drive Active",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Google Drive Cloud Backup",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Auto-fills your details on any device using your email",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Status: $syncStatusText",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BentoPrimary
                            )
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = BentoPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val success = GoogleDriveSyncManager.backupProfileToDrive(context, employee)
                            if (success) {
                                syncStatusText = "Synced Just Now ☁️"
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sync Profile to Google Drive ☁️", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }

    if (isEditingProfile) {
        ProfileEditDialog(
            employee = employee,
            onDismiss = { isEditingProfile = false },
            onSave = { updatedEmp ->
                onSaveProfile(updatedEmp)
                isEditingProfile = false
            }
        )
    }
}

@Composable
fun ProfileDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(BentoPrimaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = BentoPrimary,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ProfileEditDialog(
    employee: Employee,
    onDismiss: () -> Unit,
    onSave: (Employee) -> Unit
) {
    var name by remember { mutableStateOf(employee.name) }
    var department by remember { mutableStateOf(employee.department) }
    var designation by remember { mutableStateOf(employee.designation) }
    var email by remember { mutableStateOf(employee.email) }
    var phone by remember { mutableStateOf(employee.phone) }
    var photoUri by remember { mutableStateOf(employee.photoUri ?: "") }

    val photoPickerLauncherInDialog = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { photoUri = it.toString() }
    }

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
                .testTag("edit_profile_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Edit Profile & Photo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        photoPickerLauncherInDialog.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BentoPrimaryContainer, contentColor = BentoPrimary)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select from Google Photos / Gallery", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Or Pick Preset Avatar:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    mockAvatars.forEachIndexed { index, avatarUrl ->
                        Box(
                            modifier = Modifier
                                .clickable { photoUri = avatarUrl }
                                .testTag("select_avatar_$index")
                        ) {
                            EmployeeAvatar(
                                name = employee.name,
                                photoUri = avatarUrl,
                                size = 44.dp,
                                showVerifiedBadge = photoUri == avatarUrl
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("Department") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_dept_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_email_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

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
                            val updated = employee.copy(
                                name = name.ifBlank { employee.name },
                                department = department.ifBlank { employee.department },
                                email = email.ifBlank { employee.email },
                                phone = phone.ifBlank { employee.phone },
                                photoUri = photoUri.ifBlank { employee.photoUri }
                            )
                            onSave(updated)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_profile_btn")
                    ) {
                        Text("Save Profile")
                    }
                }
            }
        }
    }
}
