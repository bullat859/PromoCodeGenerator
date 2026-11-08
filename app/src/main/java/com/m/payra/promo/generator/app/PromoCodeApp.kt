package com.m.payra.promo.generator.app

import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m.payra.promo.generator.app.data.PromoDatabaseHelper
import com.m.payra.promo.generator.app.engine.GeneratorConfig
import com.m.payra.promo.generator.app.engine.PromoGenerator
import com.m.payra.promo.generator.app.engine.ValidationResult
import com.m.payra.promo.generator.app.export.ExportManager
import com.m.payra.promo.generator.app.ui.components.ConfigPanel
import com.m.payra.promo.generator.app.ui.components.ExportBottomSheet
import com.m.payra.promo.generator.app.ui.components.HistoryScreen
import com.m.payra.promo.generator.app.ui.components.ResultsList
import com.m.payra.promo.generator.app.ui.components.VerifyScreen
import com.m.payra.promo.generator.app.ui.theme.PromoCodeTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromoCodeApp() {
    val systemInDark = isSystemInDarkTheme()
    var isDarkTheme by remember { mutableStateOf(systemInDark) }

    PromoCodeTheme(darkTheme = isDarkTheme) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }
        val dbHelper = remember { PromoDatabaseHelper.getInstance(context) }

        var selectedNavTab by remember { mutableIntStateOf(0) } // 0: Generator, 1: History, 2: Verify & Redeem
        var config by remember { mutableStateOf(GeneratorConfig()) }
        var codes by remember { mutableStateOf<List<String>>(emptyList()) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var showExportSheet by remember { mutableStateOf(false) }

        fun runGeneration() {
            val validation = PromoGenerator.validate(config)
            if (validation is ValidationResult.Error) {
                errorMessage = validation.message
                return
            }
            errorMessage = null
            scope.launch {
                val generatedCodes = withContext(Dispatchers.Default) {
                    PromoGenerator.generate(config)
                }
                codes = generatedCodes

                // Auto-save generated batch into persistent SQLite database
                withContext(Dispatchers.IO) {
                    val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                    val campaignName = "Batch $timeStr (${generatedCodes.size} codes)"
                    dbHelper.saveBatch(campaignName, config, generatedCodes)
                }

                snackbarHostState.showSnackbar("Generated ${generatedCodes.size} promo codes & saved to History!")
            }
        }

        // NOTE: Auto-generation on launch has been removed as requested. Codes generate ONLY on tap of "GENERATE".

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "#",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 20.sp
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = when (selectedNavTab) {
                                        1 -> "Promo History & Batches"
                                        2 -> "Verify & Redeem"
                                        else -> "Promo Code Generator"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Cryptographic & Reproducible Utility",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { isDarkTheme = !isDarkTheme }) {
                            Crossfade(targetState = isDarkTheme, label = "ThemeToggle") { dark ->
                                Icon(
                                    imageVector = if (dark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Toggle Theme",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Bolt, contentDescription = "Generator") },
                        label = { Text("Generator", fontWeight = if (selectedNavTab == 0) FontWeight.Bold else FontWeight.Normal) },
                        selected = selectedNavTab == 0,
                        onClick = { selectedNavTab = 0 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.History, contentDescription = "History") },
                        label = { Text("History", fontWeight = if (selectedNavTab == 1) FontWeight.Bold else FontWeight.Normal) },
                        selected = selectedNavTab == 1,
                        onClick = { selectedNavTab = 1 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Verify & Redeem") },
                        label = { Text("Verify", fontWeight = if (selectedNavTab == 2) FontWeight.Bold else FontWeight.Normal) },
                        selected = selectedNavTab == 2,
                        onClick = { selectedNavTab = 2 }
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                when (selectedNavTab) {
                    0 -> {
                        // Generator Screen
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Status Bar Chip Row with Master Reset
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AssistChip(
                                    onClick = {
                                        config = GeneratorConfig()
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Reset all configuration settings to default!")
                                        }
                                    },
                                    label = { Text("Reset All Config", style = MaterialTheme.typography.labelSmall) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Reset All Config",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )

                                AssistChip(
                                    onClick = { },
                                    label = { Text("Ready", style = MaterialTheme.typography.labelSmall) },
                                    leadingIcon = {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(6.dp)
                                        ) {}
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Error Banner
                            if (errorMessage != null) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Error",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = errorMessage ?: "",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Adaptive Layout: 2 Columns on wide screens (>= 680dp), 1 scrollable column on phones
                            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                val isWide = this.maxWidth >= 680.dp

                                if (isWide) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        ConfigPanel(
                                            config = config,
                                            onConfigChange = { config = it },
                                            onGenerate = { runGeneration() },
                                            modifier = Modifier
                                                .weight(1.1f)
                                                .verticalScroll(rememberScrollState())
                                        )

                                        ResultsList(
                                            codes = codes,
                                            onCopySingle = { code ->
                                                ExportManager.copyToClipboard(context, code, "Promo Code")
                                                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
                                                    Toast.makeText(context, "Copied: $code", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            onCopyAll = {
                                                if (codes.isNotEmpty()) {
                                                    val allText = codes.joinToString("\n")
                                                    ExportManager.copyToClipboard(context, allText, "Promo Codes")
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar("Copied all ${codes.size} codes!")
                                                    }
                                                }
                                            },
                                            onOpenExport = { showExportSheet = true },
                                            isFlexibleHeight = true,
                                            modifier = Modifier.weight(1.3f)
                                        )
                                    }
                                } else {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        ConfigPanel(
                                            config = config,
                                            onConfigChange = { config = it },
                                            onGenerate = { runGeneration() }
                                        )

                                        ResultsList(
                                            codes = codes,
                                            onCopySingle = { code ->
                                                ExportManager.copyToClipboard(context, code, "Promo Code")
                                                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
                                                    Toast.makeText(context, "Copied: $code", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            onCopyAll = {
                                                if (codes.isNotEmpty()) {
                                                    val allText = codes.joinToString("\n")
                                                    ExportManager.copyToClipboard(context, allText, "Promo Codes")
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar("Copied all ${codes.size} codes!")
                                                    }
                                                }
                                            },
                                            onOpenExport = { showExportSheet = true },
                                            isFlexibleHeight = false
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        // History Screen
                        HistoryScreen(context = context)
                    }
                    2 -> {
                        // Verification & Redemption Screen
                        VerifyScreen(context = context)
                    }
                }
            }

            if (showExportSheet) {
                ExportBottomSheet(
                    context = context,
                    codes = codes,
                    config = config,
                    onDismiss = { showExportSheet = false }
                )
            }
        }
    }
}
