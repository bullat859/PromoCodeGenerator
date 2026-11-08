package com.m.payra.promo.generator.app.engine

enum class CasingMode(val label: String) {
    DEFAULT("Default"),
    FORCE_UPPER("Force UPPERCASE"),
    FORCE_LOWER("Force lowercase")
}

data class GeneratorConfig(
    // ⚡ Core Batch Parameters
    val quantity: Int = 10,
    val length: Int = 8,
    val seed: String = "",

    // 🔤 Character Pool & Exclusions
    val includeLower: Boolean = true,
    val customLower: String = "abcdefghijklmnopqrstuvwxyz",
    val includeUpper: Boolean = true,
    val customUpper: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZ",
    val includeNumbers: Boolean = true,
    val customNumbers: String = "0123456789",
    val includeSymbols: Boolean = false,
    val customSymbols: String = "'\"!?.,:;\$%&@~#()<>{}[]_*-+^=/|\\",
    val includeEmoji: Boolean = false,
    val customEmoji: String = "🎉🔥🎁⭐💎🚀💥⚡🏆🎯👑❤️🍀💰✨💯",
    val useCustomWhitelist: Boolean = false,
    val customWhitelist: String = "",
    val useBlacklist: Boolean = false,
    val blacklistChars: String = "",
    val avoidAmbiguous: Boolean = false,

    // 📐 Pattern & Formatting
    val prefix: String = "",
    val suffix: String = "",
    val chunkSize: Int = 0,
    val delimiter: String = "-",
    val usePatternMask: Boolean = false,
    val patternMask: String = "PROMO-###-@@@", // # = digit, @ = upper, $ = lower, * = symbol
    val casingMode: CasingMode = CasingMode.DEFAULT,

    // ⚙️ Generation & Security Rules
    val uniqueCharacters: Boolean = false,
    val shuffleCode: Boolean = true,
    val pronounceable: Boolean = false,
    val minDigits: Int = 0,
    val minUpper: Int = 0,
    val minSymbols: Int = 0,
    val enableProfanityFilter: Boolean = false,
    val appendChecksum: Boolean = false,

    // 🏷️ Campaign & Export Metadata
    val discountText: String = "",
    val expiryDays: Int = 0,
    val maxUsageCount: Int = 0,
    val includeHashInExport: Boolean = false
) {
    // Backwards Compatibility properties for legacy tests/callers
    val omitSpecificLetters: Boolean get() = useBlacklist
    val lettersToOmit: String get() = blacklistChars
}

sealed class ValidationResult {
    data object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}
