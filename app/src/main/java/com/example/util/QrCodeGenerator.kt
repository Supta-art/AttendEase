package com.example.util

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

object QrCodeGenerator {

    fun generateQrBitmap(
        content: String,
        width: Int = 512,
        height: Int = 512,
        primaryColorHex: Int = AndroidColor.BLACK,
        backgroundColorHex: Int = AndroidColor.WHITE
    ): ImageBitmap? {
        if (content.isBlank()) return null
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height)
            val matrixWidth = bitMatrix.width
            val matrixHeight = bitMatrix.height
            val pixels = IntArray(matrixWidth * matrixHeight)

            for (y in 0 until matrixHeight) {
                val offset = y * matrixWidth
                for (x in 0 until matrixWidth) {
                    pixels[offset + x] = if (bitMatrix.get(x, y)) primaryColorHex else backgroundColorHex
                }
            }

            val bitmap = Bitmap.createBitmap(matrixWidth, matrixHeight, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, matrixWidth, 0, 0, matrixWidth, matrixHeight)
            bitmap.asImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
