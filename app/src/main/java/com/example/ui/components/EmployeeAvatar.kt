package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.IndigoSecondary

@Composable
fun EmployeeAvatar(
    name: String,
    photoUri: String?,
    size: Dp = 48.dp,
    showVerifiedBadge: Boolean = true,
    modifier: Modifier = Modifier
) {
    val initials = name.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()

    Box(modifier = modifier.size(size)) {
        if (!photoUri.isNull_or_blank_safe()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(photoUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Profile Photo of $name",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )
        } else {
            // High-contrast gradient fallback avatar
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(IndigoPrimary, IndigoSecondary)
                        )
                    )
            ) {
                Text(
                    text = initials.ifEmpty { "E" },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.4f).sp
                )
            }
        }

        if (showVerifiedBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(size * 0.32f)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = "Identity Verified",
                    tint = EmeraldSuccess,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

private fun String?.isNull_or_blank_safe(): Boolean {
    return this == null || this.trim().isEmpty()
}
