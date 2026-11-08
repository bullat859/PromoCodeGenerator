package com.m.payra.promo.generator.app.export

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.m.payra.promo.generator.app.engine.GeneratorConfig
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExportHelper {

    fun generatePdf(
        context: Context,
        codes: List<String>,
        config: GeneratorConfig = GeneratorConfig()
    ): File {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val marginX = 36f
        val contentWidth = pageWidth - (marginX * 2)

        val titlePaint = Paint().apply {
            color = Color.parseColor("#1E1E24")
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subPaint = Paint().apply {
            color = Color.parseColor("#6B7280")
            textSize = 9f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }

        val indexPaint = Paint().apply {
            color = Color.parseColor("#008080")
            textSize = 10f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }

        val codePaint = Paint().apply {
            color = Color.parseColor("#24252A")
            textSize = 10f
            typeface = Typeface.MONOSPACE
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.parseColor("#E5E7EB")
            strokeWidth = 1f
        }

        val maxCodeLen = codes.maxOfOrNull { it.length } ?: 0
        val numCols = if (maxCodeLen <= 24) 2 else 1
        val colWidth = contentWidth / numCols

        val rowHeight = 24f
        val maxY = 800f

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        fun drawPageHeader(isFirstPage: Boolean): Float {
            var y = 40f
            if (isFirstPage) {
                canvas.drawText("Promo Code Generator — Export Report", marginX, y, titlePaint)
                y += 18f
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                var metaStr = "Generated: ${dateFormat.format(Date())}   |   Total Codes: ${codes.size}"
                if (config.discountText.isNotBlank()) metaStr += "   |   Discount: ${config.discountText}"
                if (config.expiryDays > 0) metaStr += "   |   Valid: ${config.expiryDays} days"
                canvas.drawText(metaStr, marginX, y, subPaint)
                y += 14f
            } else {
                canvas.drawText("Promo Code Export Report (Page $pageNumber)", marginX, y, subPaint)
                y += 14f
            }
            canvas.drawLine(marginX, y, marginX + contentWidth, y, linePaint)
            return y + 16f
        }

        var currentY = drawPageHeader(isFirstPage = true)
        var currentCol = 0

        for (i in codes.indices) {
            if (currentY + rowHeight > maxY) {
                if (currentCol < numCols - 1) {
                    currentCol++
                    currentY = if (pageNumber == 1) 88f else 70f
                } else {
                    canvas.drawText("Page $pageNumber", marginX + contentWidth - 40f, 825f, subPaint)
                    document.finishPage(page)

                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = document.startPage(pageInfo)
                    canvas = page.canvas

                    currentCol = 0
                    currentY = drawPageHeader(isFirstPage = false)
                }
            }

            val colX = marginX + (currentCol * colWidth)
            val indexStr = String.format(Locale.US, "#%03d", i + 1)
            val currentCode = codes[i]
            canvas.drawText(indexStr, colX, currentY, indexPaint)
            canvas.drawText(currentCode, colX + 44f, currentY, codePaint)

            val qrBitmap: Bitmap? = try {
                QrCodeGenerator.generateQrBitmap(currentCode, size = 32)
            } catch (_: Exception) {
                null
            }

            if (qrBitmap != null) {
                val qrRect = Rect((colX + colWidth - 28f).toInt(), (currentY - 10f).toInt(), (colX + colWidth - 12f).toInt(), (currentY + 6f).toInt())
                canvas.drawBitmap(qrBitmap, null, qrRect, null)
                qrBitmap.recycle()
            }

            currentY += rowHeight
        }

        canvas.drawText("Page $pageNumber", marginX + contentWidth - 40f, 825f, subPaint)
        document.finishPage(page)

        val outputDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val outputFile = File(outputDir, "promo_codes_${System.currentTimeMillis()}.pdf")
        FileOutputStream(outputFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        return outputFile
    }
}
