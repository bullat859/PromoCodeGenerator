package com.m.payra.promo.generator.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m.payra.promo.generator.app.ui.theme.CodeHighlightGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ResultsList(
    codes: List<String>,
    onCopySingle: (String) -> Unit,
    onCopyAll: () -> Unit,
    onOpenExport: () -> Unit,
    modifier: Modifier = Modifier,
    isFlexibleHeight: Boolean = false
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Output Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                text = "${codes.size} Codes",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            leadingIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onCopyAll,
                        enabled = codes.isNotEmpty(),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 12.dp,
                            vertical = 6.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy All",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Copy All", style = MaterialTheme.typography.labelMedium)
                    }

                    FilledTonalButton(
                        onClick = onOpenExport,
                        enabled = codes.isNotEmpty(),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 12.dp,
                            vertical = 6.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Export",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Export", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            if (codes.isNotEmpty()) {
                Text(
                    text = "Tip: Hold & slide on any code to manually select or copy text.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val boxModifier = if (isFlexibleHeight) {
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
            } else {
                Modifier
                    .fillMaxWidth()
                    .height(360.dp)
            }

            Surface(
                modifier = boxModifier,
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                tonalElevation = 1.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    if (codes.isEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.VpnKey,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Click GENERATE CODES to build your batch",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        SelectionContainer {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                itemsIndexed(codes) { index, code ->
                                    CodeRow(
                                        index = index + 1,
                                        code = code,
                                        onCopy = { onCopySingle(code) }
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

@Composable
private fun CodeRow(
    index: Int,
    code: String,
    onCopy: () -> Unit
) {
    var copied by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = String.format(Locale.US, "#%03d", index),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(44.dp)
            )

            Text(
                text = code,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = {
                    onCopy()
                    copied = true
                    scope.launch {
                        delay(1800)
                        copied = false
                    }
                },
                modifier = Modifier.size(34.dp)
            ) {
                AnimatedContent(targetState = copied, label = "copyAnim") { isCopied ->
                    if (isCopied) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Copied",
                            tint = CodeHighlightGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy code",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }
    }
}
