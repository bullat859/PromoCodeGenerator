package com.m.payra.promo.generator.app.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m.payra.promo.generator.app.data.CampaignRecord
import com.m.payra.promo.generator.app.data.CodeRecord
import com.m.payra.promo.generator.app.data.PromoDatabaseHelper
import com.m.payra.promo.generator.app.export.ExportManager

@Composable
fun HistoryScreen(
    context: Context,
    modifier: Modifier = Modifier
) {
    val dbHelper = remember { PromoDatabaseHelper.getInstance(context) }
    var campaigns by remember { mutableStateOf<List<CampaignRecord>>(emptyList()) }
    var expandedCampaignId by remember { mutableStateOf<Long?>(null) }
    var codesForExpanded by remember { mutableStateOf<List<CodeRecord>>(emptyList()) }

    fun refresh() {
        campaigns = dbHelper.getAllCampaigns()
        if (expandedCampaignId != null) {
            codesForExpanded = dbHelper.getCodesForCampaign(expandedCampaignId!!)
        }
    }

    LaunchedEffect(Unit) {
        refresh()
    }

    if (campaigns.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
                Text(
                    text = "No Saved Batches Yet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Generated promo code batches will automatically appear here.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(campaigns, key = { it.id }) { campaign ->
                val isExpanded = expandedCampaignId == campaign.id

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isExpanded) {
                                        expandedCampaignId = null
                                    } else {
                                        expandedCampaignId = campaign.id
                                        codesForExpanded = dbHelper.getCodesForCampaign(campaign.id)
                                    }
                                },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = campaign.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${campaign.formattedDate} • ${campaign.quantity} codes",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = campaign.configSummary,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        val codes = dbHelper.getCodesForCampaign(campaign.id).map { it.code }
                                        val file = ExportManager.exportToFile(context, codes, ExportManager.ExportFormat.CSV)
                                        ExportManager.shareExportedFile(context, file, ExportManager.ExportFormat.CSV.mimeType)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share CSV",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        dbHelper.deleteCampaign(campaign.id)
                                        if (expandedCampaignId == campaign.id) {
                                            expandedCampaignId = null
                                        }
                                        refresh()
                                        Toast.makeText(context, "Deleted batch", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Batch",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "Toggle Expand",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        AnimatedVisibility(visible = isExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                        .padding(8.dp)
                                ) {
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        items(codesForExpanded, key = { it.id }) { codeItem ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        MaterialTheme.colorScheme.surface,
                                                        RoundedCornerShape(6.dp)
                                                    )
                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = codeItem.code,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontFamily = FontFamily.Monospace,
                                                        fontWeight = FontWeight.SemiBold
                                                    ),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )

                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    if (codeItem.isRedeemed) {
                                                        AssistChip(
                                                            onClick = { },
                                                            label = { Text("Redeemed", fontSize = 10.sp) },
                                                            colors = AssistChipDefaults.assistChipColors(
                                                                containerColor = Color(0x26FFA500),
                                                                labelColor = Color(0xFFFF9800)
                                                            ),
                                                            modifier = Modifier.height(24.dp)
                                                        )
                                                    } else {
                                                        AssistChip(
                                                            onClick = { },
                                                            label = { Text("Active", fontSize = 10.sp) },
                                                            colors = AssistChipDefaults.assistChipColors(
                                                                containerColor = Color(0x2610B981),
                                                                labelColor = Color(0xFF10B981)
                                                            ),
                                                            modifier = Modifier.height(24.dp)
                                                        )
                                                    }

                                                    IconButton(
                                                        onClick = {
                                                            ExportManager.copyToClipboard(context, codeItem.code, "Promo Code")
                                                            Toast.makeText(context, "Copied: ${codeItem.code}", Toast.LENGTH_SHORT).show()
                                                        },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.ContentCopy,
                                                            contentDescription = "Copy",
                                                            modifier = Modifier.size(15.dp),
                                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
