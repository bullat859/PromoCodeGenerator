package com.m.payra.promo.generator.app.engine

data class GeneratorPreset(
    val id: String,
    val name: String,
    val description: String,
    val config: GeneratorConfig
)

object PresetManager {

    val BUILT_IN_PRESETS = listOf(
        GeneratorPreset(
            id = "default",
            name = "Standard Alphanumeric",
            description = "8-character upper/lower + numbers",
            config = GeneratorConfig()
        ),
        GeneratorPreset(
            id = "black_friday",
            name = "Black Friday Sale",
            description = "BF2026-XXXX-XXXX format",
            config = GeneratorConfig(
                prefix = "BF2026-",
                chunkSize = 4,
                delimiter = "-",
                length = 8,
                includeUpper = true,
                includeNumbers = true,
                includeLower = false,
                discountText = "30% OFF",
                expiryDays = 30
            )
        ),
        GeneratorPreset(
            id = "voucher",
            name = "Store Voucher Code",
            description = "SAVE-####-@@@@ pattern mask",
            config = GeneratorConfig(
                usePatternMask = true,
                patternMask = "SAVE-####-@@@@",
                discountText = "$25 Voucher",
                maxUsageCount = 1,
                appendChecksum = true
            )
        ),
        GeneratorPreset(
            id = "pronounceable",
            name = "Phone / Support Code",
            description = "Easy-to-read pronounceable code",
            config = GeneratorConfig(
                pronounceable = true,
                length = 8,
                chunkSize = 4,
                casingMode = CasingMode.FORCE_UPPER,
                avoidAmbiguous = true,
                enableProfanityFilter = true
            )
        ),
        GeneratorPreset(
            id = "secure_token",
            name = "Secure Token / Hash",
            description = "16-char crypto unique token with checksum",
            config = GeneratorConfig(
                length = 16,
                includeLower = true,
                includeUpper = true,
                includeNumbers = true,
                includeSymbols = true,
                appendChecksum = true,
                includeHashInExport = true
            )
        )
    )
}
