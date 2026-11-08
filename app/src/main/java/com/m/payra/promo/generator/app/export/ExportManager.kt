package com.m.payra.promo.generator.app.export

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.m.payra.promo.generator.app.engine.GeneratorConfig
import java.io.File
import java.io.FileWriter
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object ExportManager {

    enum class ExportFormat(val extension: String, val mimeType: String, val label: String) {
        CSV("csv", "text/csv", "CSV Spreadsheet (.csv)"),
        TSV("tsv", "text/tab-separated-values", "TSV File (.tsv)"),
        TXT("txt", "text/plain", "Plain Text (.txt)"),
        SHEETS("xls", "application/vnd.ms-excel", "Excel / Google Sheets (.xls)"),
        PDF("pdf", "application/pdf", "PDF Document (.pdf)")
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun exportToFile(
        context: Context,
        codes: List<String>,
        format: ExportFormat,
        config: GeneratorConfig = GeneratorConfig()
    ): File {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(exportDir, "promo_codes_$timestamp.${format.extension}")

        val hasDiscount = config.discountText.isNotBlank()
        val hasExpiry = config.expiryDays > 0
        val hasUsage = config.maxUsageCount > 0
        val hasHash = config.includeHashInExport

        val expiryDateStr = if (hasExpiry) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, config.expiryDays)
            SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
        } else "N/A"

        val discountStr = if (hasDiscount) config.discountText else "N/A"
        val usageStr = if (hasUsage) config.maxUsageCount.toString() else "Unlimited"

        when (format) {
            ExportFormat.CSV -> {
                FileWriter(file).use { writer ->
                    val headers = ArrayList<String>()
                    headers.add("Index")
                    headers.add("Promo Code")
                    if (hasDiscount) headers.add("Discount")
                    if (hasExpiry) headers.add("Expiry Date")
                    if (hasUsage) headers.add("Max Usage")
                    if (hasHash) headers.add("SHA-256 Hash")

                    writer.append(headers.joinToString(",") + "\n")

                    codes.forEachIndexed { index, code ->
                        val row = ArrayList<String>()
                        row.add("${index + 1}")
                        row.add("\"${code.replace("\"", "\"\"")}\"")
                        if (hasDiscount) row.add("\"$discountStr\"")
                        if (hasExpiry) row.add("\"$expiryDateStr\"")
                        if (hasUsage) row.add("\"$usageStr\"")
                        if (hasHash) row.add("\"${sha256(code)}\"")
                        writer.append(row.joinToString(",") + "\n")
                    }
                }
            }
            ExportFormat.TSV -> {
                FileWriter(file).use { writer ->
                    val headers = ArrayList<String>()
                    headers.add("Index")
                    headers.add("Promo Code")
                    if (hasDiscount) headers.add("Discount")
                    if (hasExpiry) headers.add("Expiry Date")
                    if (hasUsage) headers.add("Max Usage")
                    if (hasHash) headers.add("SHA-256 Hash")

                    writer.append(headers.joinToString("\t") + "\n")

                    codes.forEachIndexed { index, code ->
                        val row = ArrayList<String>()
                        row.add("${index + 1}")
                        row.add(code)
                        if (hasDiscount) row.add(discountStr)
                        if (hasExpiry) row.add(expiryDateStr)
                        if (hasUsage) row.add(usageStr)
                        if (hasHash) row.add(sha256(code))
                        writer.append(row.joinToString("\t") + "\n")
                    }
                }
            }
            ExportFormat.TXT -> {
                FileWriter(file).use { writer ->
                    codes.forEach { code ->
                        if (hasHash) {
                            writer.append("$code | Hash: ${sha256(code)}\n")
                        } else {
                            writer.append("$code\n")
                        }
                    }
                }
            }
            ExportFormat.SHEETS -> {
                FileWriter(file).use { writer ->
                    writer.append("<html xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:x=\"urn:schemas-microsoft-com:office:excel\">")
                    writer.append("<head><meta charset=\"utf-8\"></head><body><table>")
                    writer.append("<thead><tr><th>Index</th><th>Promo Code</th>")
                    if (hasDiscount) writer.append("<th>Discount</th>")
                    if (hasExpiry) writer.append("<th>Expiry Date</th>")
                    if (hasUsage) writer.append("<th>Max Usage</th>")
                    if (hasHash) writer.append("<th>SHA-256 Hash</th>")
                    writer.append("</tr></thead><tbody>")

                    codes.forEachIndexed { index, code ->
                        writer.append("<tr><td>${index + 1}</td><td>$code</td>")
                        if (hasDiscount) writer.append("<td>$discountStr</td>")
                        if (hasExpiry) writer.append("<td>$expiryDateStr</td>")
                        if (hasUsage) writer.append("<td>$usageStr</td>")
                        if (hasHash) writer.append("<td>${sha256(code)}</td>")
                        writer.append("</tr>")
                    }
                    writer.append("</tbody></table></body></html>")
                }
            }
            ExportFormat.PDF -> {
                return PdfExportHelper.generatePdf(context, codes, config)
            }
        }

        return file
    }

    fun shareExportedFile(context: Context, file: File, mimeType: String) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Exported Promo Codes")
            putExtra(Intent.EXTRA_TEXT, "Here is your exported list of promo codes.")
            clipData = ClipData.newRawUri("Promo Codes", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "Share Promo Codes via...")
        chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun copyToClipboard(context: Context, text: String, label: String = "Promo Code") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
    }
}
