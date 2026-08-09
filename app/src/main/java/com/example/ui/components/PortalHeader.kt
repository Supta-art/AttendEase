package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Employee
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryContainer
import com.example.ui.theme.BentoSuccess
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.IndigoSecondary
import com.example.util.DateUtils

@Composable
fun PortalHeader(
    activePortal: String, // "SELECT_PORTAL", "ADMIN" or "EMPLOYEE"
    activeEmployee: Employee?,
    onPortalChanged: (String) -> Unit,
    onOpenQrScanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = statusBarPadding + 8.dp,
                    bottom = 12.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left App Logo & Title Branding
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
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
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "AttendEase",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 17.sp
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Active Portal Chip
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (activePortal == "ADMIN") BentoPrimaryContainer else BentoSuccess.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (activePortal == "ADMIN") Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (activePortal == "ADMIN") BentoPrimary else BentoSuccess,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (activePortal == "ADMIN") "ADMIN" else "STAFF",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (activePortal == "ADMIN") BentoPrimary else BentoSuccess
                                )
                            }
                        }
                    }

                    Text(
                        text = if (activePortal == "EMPLOYEE" && activeEmployee != null) {
                            "${activeEmployee.name} • ${activeEmployee.department}"
                        } else {
                            DateUtils.getFormattedDate(DateUtils.getTodayDateString())
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
            }

            // Right Action Buttons (QR Scanner & Logout / Switch Portal)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Quick QR Scanner Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { onOpenQrScanner() }
                        .testTag("header_qr_scanner_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scan QR Code",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (activeEmployee != null && activePortal == "EMPLOYEE") {
                    EmployeeAvatar(
                        name = activeEmployee.name,
                        photoUri = activeEmployee.photoUri,
                        size = 36.dp,
                        showVerifiedBadge = true
                    )
                }

                // Return / Logout to Auth Portal
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onPortalChanged("SELECT_PORTAL") }
                        .testTag("header_switch_role_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout / Switch Portal",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
