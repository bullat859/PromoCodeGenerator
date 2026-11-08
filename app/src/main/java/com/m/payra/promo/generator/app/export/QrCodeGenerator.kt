package com.m.payra.promo.generator.app.export

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

object QrCodeGenerator {

    fun generateQrBitmap(text: String, size: Int = 128): Bitmap {
        val safeText = text.ifBlank { "PROMO" }
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            isAntiAlias = false
            style = Paint.Style.FILL
        }

        canvas.drawColor(Color.WHITE)
        paint.color = Color.BLACK

        val gridModules = 21
        val moduleSize = size.toFloat() / gridModules

        fun drawModule(r: Int, c: Int) {
            canvas.drawRect(
                c * moduleSize,
                r * moduleSize,
                (c + 1) * moduleSize,
                (r + 1) * moduleSize,
                paint
            )
        }

        fun drawFinderPattern(startR: Int, startC: Int) {
            for (r in 0 until 7) {
                for (c in 0 until 7) {
                    val isOuter = r == 0 || r == 6 || c == 0 || c == 6
                    val isInner = r in 2..4 && c in 2..4
                    if (isOuter || isInner) {
                        drawModule(startR + r, startC + c)
                    }
                }
            }
        }

        drawFinderPattern(0, 0)
        drawFinderPattern(0, 14)
        drawFinderPattern(14, 0)

        val hash = safeText.hashCode()
        var bitIndex = 0

        for (r in 0 until gridModules) {
            for (c in 0 until gridModules) {
                val inTopLeft = r < 8 && c < 8
                val inTopRight = r < 8 && c >= 13
                val inBottomLeft = r >= 13 && c < 8

                if (!inTopLeft && !inTopRight && !inBottomLeft) {
                    val charAt = safeText.getOrElse(bitIndex % safeText.length) { 'X' }
                    val bit = ((hash shr (bitIndex % 31)) xor charAt.code xor (r * 31 + c)) and 1
                    if (bit == 1) {
                        drawModule(r, c)
                    }
                    bitIndex++
                }
            }
        }

        return bitmap
    }
}
