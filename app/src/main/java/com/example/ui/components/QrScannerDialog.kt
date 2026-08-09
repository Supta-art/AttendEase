package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.model.Employee
import com.example.ui.theme.EmeraldSuccess
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.util.concurrent.Executors

@Composable
fun QrScannerDialog(
    employees: List<Employee>,
    onDismiss: () -> Unit,
    onQrScanned: (String) -> Unit
) {
    var manualToken by remember { mutableStateOf("") }
    var scannerMode by remember { mutableStateOf("SCANNER") } // "SCANNER" or "EMPLOYEE_LIST"

    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
    val laserYPosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 180f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("qr_scanner_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Daily QR Check-In Scanner",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_qr_scanner")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (scannerMode == "SCANNER") {
                    // Live Camera Scanner Box with Reticle & Laser Overlay
                    Box(
                        contentAlignment = Alignment.TopCenter,
                        modifier = Modifier
                            .size(220.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF0F172A))
                            .border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp))
                    ) {
                        // Live Camera Feed
                        LiveCameraScannerView(onQrScanned = onQrScanned)

                        // Scanner reticle frame
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .align(Alignment.Center)
                                .border(2.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        )

                        // Animated Laser line
                        Box(
                            modifier = Modifier
                                .padding(top = laserYPosition.dp)
                                .fillMaxWidth()
                                .height(3.dp)
                                .background(EmeraldSuccess)
                        )

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Point camera at QR Code",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Scan an employee's daily QR code or select below to simulate scan",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (employees.size == 1) {
                                    val emp = employees.first()
                                    val dynamicToken = "${emp.qrToken}-${System.currentTimeMillis() / 60000L}"
                                    onQrScanned(dynamicToken)
                                } else {
                                    scannerMode = "EMPLOYEE_LIST"
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("select_employee_scan_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.TouchApp, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (employees.size == 1) "Quick Pass Check-In" else "Select Employee Pass",
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = manualToken,
                        onValueChange = { manualToken = it },
                        label = { Text("Or Enter QR Token manually") },
                        placeholder = { Text("e.g. ATT-EMP-1002-TOKEN") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manual_qr_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (manualToken.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onQrScanned(manualToken) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("submit_manual_token_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Submit Scan Token")
                        }
                    }
                } else {
                    // Employee selection list for quick testing scan
                    Text(
                        text = "Tap employee to scan their QR Pass:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    ) {
                        items(employees) { emp ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onQrScanned(emp.qrToken) }
                                    .testTag("scan_item_${emp.employeeId}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        EmployeeAvatar(name = emp.name, photoUri = emp.photoUri, size = 36.dp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = emp.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${emp.department} • ${emp.employeeId}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                    }

                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Scan",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(onClick = { scannerMode = "SCANNER" }) {
                        Text("Back to Viewfinder")
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveCameraScannerView(
    onQrScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    if (!hasCameraPermission) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Camera Permission Required",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Allow Camera", fontSize = 11.sp)
            }
        }
    } else {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    val executor = Executors.newSingleThreadExecutor()
                    imageAnalysis.setAnalyzer(executor, QrCodeAnalyzer(onQrScanned))

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@OptIn(ExperimentalGetImage::class)
private class QrCodeAnalyzer(
    private val onQrScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader().apply {
        setHints(
            mapOf(
                DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE)
            )
        )
    }

    private var isScanned = false

    override fun analyze(imageProxy: ImageProxy) {
        if (isScanned) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null && mediaImage.format == ImageFormat.YUV_420_888) {
            val plane = mediaImage.planes[0]
            val buffer = plane.buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)

            val width = mediaImage.width
            val height = mediaImage.height

            val source = PlanarYUVLuminanceSource(
                bytes, width, height, 0, 0, width, height, false
            )
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

            try {
                val result = reader.decodeWithState(binaryBitmap)
                val qrText = result.text
                if (!qrText.isNullOrBlank()) {
                    isScanned = true
                    onQrScanned(qrText)
                }
            } catch (e: Exception) {
                // No QR code found in frame
            } finally {
                reader.reset()
                imageProxy.close()
            }
        } else {
            imageProxy.close()
        }
    }
}
