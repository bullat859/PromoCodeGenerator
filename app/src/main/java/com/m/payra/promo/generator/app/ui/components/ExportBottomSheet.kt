package com.m.payra.promo.generator.app.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m.payra.promo.generator.app.engine.GeneratorConfig
import com.m.payra.promo.generator.app.export.ExportManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportBottomSheet(
    context: Context,
    codes: List<String>,
    config: GeneratorConfig = GeneratorConfig(),
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var isExporting by remember { mutableStateOf(false) }

    fun performExport(format: ExportManager.ExportFormat) {
        if (isExporting) return
        isExporting = true
        scope.launch {
            try {
                val file = withContext(Dispatchers.IO) {
                    ExportManager.exportToFile(context, codes, format, config)
                }
                ExportManager.shareExportedFile(context, file, format.mimeType)
                onDismiss()
            } catch (e: Exception) {
                Toast.makeText(context, "Export failed: ${e.localizedMessage ?: "Unknown error"}", Toast.LENGTH_LONG).show()
            } finally {
                isExporting = false
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = { if (!isExporting) onDismiss() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "EXPORT PROMO CODES",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 0.5.sp
                )
                if (isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                }
            }

            Text(
                text = "Choose your preferred export format. Supports metadata, hashes, and printable PDF documents.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            ExportOptionRow(
                icon = Icons.Default.TableChart,
                title = "CSV Spreadsheet (.csv)",
                subtitle = "Comma-delimited format for spreadsheets & databases",
                enabled = !isExporting,
                onClick = { performExport(ExportManager.ExportFormat.CSV) }
            )

            ExportOptionRow(
                icon = Icons.Default.TableChart,
                title = "TSV File (.tsv)",
                subtitle = "Tab-delimited format for quick copy & import",
                enabled = !isExporting,
                onClick = { performExport(ExportManager.ExportFormat.TSV) }
            )

            ExportOptionRow(
                icon = Icons.Default.TableChart,
                title = "Excel / Google Sheets (.xls)",
                subtitle = "Directly importable into Google Sheets & Microsoft Excel",
                enabled = !isExporting,
                onClick = { performExport(ExportManager.ExportFormat.SHEETS) }
            )

            ExportOptionRow(
                icon = Icons.Default.PictureAsPdf,
                title = "PDF Document (.pdf)",
                subtitle = "Clean printable document rendered via Android PdfDocument",
                enabled = !isExporting,
                onClick = { performExport(ExportManager.ExportFormat.PDF) }
            )

            ExportOptionRow(
                icon = Icons.Default.TextSnippet,
                title = "Plain Text (.txt)",
                subtitle = "Simple line-by-line list of promo codes",
                enabled = !isExporting,
                onClick = { performExport(ExportManager.ExportFormat.TXT) }
            )
        }
    }
}

@Composable
private fun ExportOptionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
