package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.QrCodeGenerator

@Composable
fun QrCodeImageView(
    employeeName: String,
    employeeId: String,
    qrToken: String,
    qrSize: Dp = 220.dp,
    modifier: Modifier = Modifier,
    onShareClicked: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val qrBitmap = remember(qrToken) {
        QrCodeGenerator.generateQrBitmap(qrToken, width = 600, height = 600)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode2,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Official Daily Attendance QR Pass",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "$employeeName ($employeeId)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(qrSize)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap,
                        contentDescription = "Daily Check-In QR Code for $employeeName",
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = "Unable to render QR",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Red
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "TOKEN: $qrToken",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(qrToken))
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Copy Token", fontSize = 12.sp)
                }

                if (onShareClicked != null) {
                    OutlinedButton(
                        onClick = onShareClicked,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Pass",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text("Share Badge", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
