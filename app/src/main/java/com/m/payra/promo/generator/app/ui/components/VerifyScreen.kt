package com.m.payra.promo.generator.app.ui.components

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m.payra.promo.generator.app.data.BatchVerificationStats
import com.m.payra.promo.generator.app.data.CodeVerificationStatus
import com.m.payra.promo.generator.app.data.PromoDatabaseHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VerifyScreen(
    context: Context,
    modifier: Modifier = Modifier
) {
    val dbHelper = remember { PromoDatabaseHelper.getInstance(context) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Single, 1: Batch

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Single Code Verifier", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Batch Auditor", fontWeight = FontWeight.Bold) }
            )
        }

        if (selectedTab == 0) {
            SingleCodeVerifier(context, dbHelper)
        } else {
            BatchCodeAuditor(dbHelper)
        }
    }
}

@Composable
private fun SingleCodeVerifier(
    context: Context,
    dbHelper: PromoDatabaseHelper
) {
    var inputCode by remember { mutableStateOf("") }
    var verificationResult by remember { mutableStateOf<CodeVerificationStatus?>(null) }

    fun verify() {
        if (inputCode.trim().isNotEmpty()) {
            verificationResult = dbHelper.verifyCode(inputCode)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "VALIDATE & REDEEM PROMO CODE",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Enter a customer code to check its authenticity, database existence, and redemption status.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputCode,
                    onValueChange = { inputCode = it },
                    label = { Text("Enter Promo Code") },
                    placeholder = { Text("e.g. BF-8K92-2026") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { verify() }),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = clipboard.primaryClip
                                if (clip != null && clip.itemCount > 0) {
                                    val text = clip.getItemAt(0).text?.toString() ?: ""
                                    inputCode = text.trim()
                                    verify()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Paste",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            }

            Button(
                onClick = { verify() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("VERIFY CODE", fontWeight = FontWeight.Bold)
            }

            AnimatedVisibility(visible = verificationResult != null) {
                val res = verificationResult
                if (res != null) {
                    val statusColor = when {
                        !res.existsInDatabase && !res.isChecksumValid -> Color(0xFFEF4444)
                        res.isRedeemed -> Color(0xFFFF9800)
                        else -> Color(0xFF10B981)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, statusColor, RoundedCornerShape(10.dp))
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = when {
                                        !res.existsInDatabase && !res.isChecksumValid -> Icons.Default.Error
                                        res.isRedeemed -> Icons.Default.Warning
                                        else -> Icons.Default.CheckCircle
                                    },
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = when {
                                        !res.existsInDatabase && !res.isChecksumValid -> "INVALID CODE"
                                        !res.existsInDatabase && res.isChecksumValid -> "VALID CHECKSUM (NOT IN LOCAL DB)"
                                        res.isRedeemed -> "ALREADY REDEEMED"
                                        else -> "VALID & ACTIVE"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor,
                                    fontSize = 15.sp
                                )
                            }

                            if (res.existsInDatabase) {
                                Text(
                                    text = "Campaign: ${res.campaignName ?: "Unknown"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (res.isChecksumValid) {
                                Text(
                                    text = "Mathematical Checksum: Passed (${res.checksumTypeDetected})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF10B981)
                                )
                            }

                            if (res.isRedeemed && res.redeemedAt != null) {
                                val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(res.redeemedAt))
                                Text(
                                    text = "Redeemed on: $dateStr",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFFF9800)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            if (res.existsInDatabase) {
                                if (!res.isRedeemed) {
                                    Button(
                                        onClick = {
                                            dbHelper.redeemCode(res.code)
                                            verificationResult = dbHelper.verifyCode(res.code)
                                            Toast.makeText(context, "Code marked as REDEEMED!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(imageVector = Icons.Default.Done, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("MARK AS REDEEMED", fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = {
                                            dbHelper.unredeemCode(res.code)
                                            verificationResult = dbHelper.verifyCode(res.code)
                                            Toast.makeText(context, "Redemption cancelled!", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("CANCEL REDEMPTION (MAKE ACTIVE)")
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

@Composable
private fun BatchCodeAuditor(
    dbHelper: PromoDatabaseHelper
) {
    var rawBatchInput by remember { mutableStateOf("") }
    var statsResult by remember { mutableStateOf<BatchVerificationStats?>(null) }

    fun runBatchAudit() {
        val codes = rawBatchInput
            .split("\n", ",", "\t")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (codes.isNotEmpty()) {
            statsResult = dbHelper.verifyBatch(codes)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "BATCH PROMO CODE AUDITOR",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Paste a list of promo codes (separated by new lines or commas) to audit authenticity and redemption counts in bulk.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = rawBatchInput,
                onValueChange = { rawBatchInput = it },
                label = { Text("Paste Codes Here") },
                placeholder = { Text("CODE-001\nCODE-002\nCODE-003...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
            )

            Button(
                onClick = { runBatchAudit() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("AUDIT BATCH", fontWeight = FontWeight.Bold)
            }

            AnimatedVisibility(visible = statsResult != null) {
                val stats = statsResult
                if (stats != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Audit Summary (Total: ${stats.totalChecked} codes)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetricCard("Active", stats.unredeemed.toString(), Color(0xFF10B981), Modifier.weight(1f))
                            MetricCard("Redeemed", stats.alreadyRedeemed.toString(), Color(0xFFFF9800), Modifier.weight(1f))
                            MetricCard("Invalid/Other", stats.notFoundInDb.toString(), Color(0xFFEF4444), Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 11.sp, color = color, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 18.sp, color = color, fontWeight = FontWeight.Bold)
        }
    }
}
