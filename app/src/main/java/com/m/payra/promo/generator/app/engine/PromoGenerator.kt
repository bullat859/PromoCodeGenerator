package com.m.payra.promo.generator.app.engine

import java.security.SecureRandom
import java.util.Random
import kotlin.math.pow

object PromoGenerator {

    private val AMBIGUOUS_CHARS = setOf('i', 'l', '1', 'I', 'o', '0', 'O')
    private val VOWELS = listOf('a', 'e', 'i', 'o', 'u', 'A', 'E', 'I', 'O', 'U')
    private val CONSONANTS = listOf(
        'b', 'c', 'd', 'f', 'g', 'h', 'j', 'k', 'm', 'n', 'p', 'r', 's', 't', 'v', 'w', 'x', 'y', 'z',
        'B', 'C', 'D', 'F', 'G', 'H', 'J', 'K', 'M', 'N', 'P', 'R', 'S', 'T', 'V', 'W', 'X', 'Y', 'Z'
    )
    val EMOJI_POOL = listOf("🎉", "🔥", "🎁", "⭐", "💎", "🚀", "💥", "⚡", "🏆", "🎯", "👑", "❤️", "🍀", "💰", "✨", "💯")

    fun parseEmojiTokens(text: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        while (i < text.length) {
            val codePoint = text.codePointAt(i)
            val charCount = Character.charCount(codePoint)
            tokens.add(text.substring(i, i + charCount))
            i += charCount
        }
        return tokens.distinct()
    }

    fun isEmojiCodePoint(cp: Int): Boolean {
        return (cp in 0x1F300..0x1F5FF) ||
               (cp in 0x1F600..0x1F64F) ||
               (cp in 0x1F680..0x1F6FF) ||
               (cp in 0x1F700..0x1F77F) ||
               (cp in 0x1F780..0x1F7FF) ||
               (cp in 0x1F800..0x1F8FF) ||
               (cp in 0x1F900..0x1F9FF) ||
               (cp in 0x1FA00..0x1FA6F) ||
               (cp in 0x1FA70..0x1FAFF) ||
               (cp in 0x2600..0x26FF) ||
               (cp in 0x2700..0x27BF) ||
               (cp in 0x2B00..0x2BFF) ||
               (cp in 0x2300..0x23FF) ||
               (cp in 0x1F1E6..0x1F1FF) ||
               cp == 0x200D ||
               cp == 0xFE0F
    }

    fun isEmojiToken(token: String): Boolean {
        if (token.isEmpty()) return false
        for (i in 0 until token.length) {
            val ch = token[i]
            if (ch in 'a'..'z' || ch in 'A'..'Z' || ch in '0'..'9') return false
        }
        var hasEmojiCodePoint = false
        var idx = 0
        while (idx < token.length) {
            val cp = token.codePointAt(idx)
            if (isEmojiCodePoint(cp)) {
                hasEmojiCodePoint = true
            }
            idx += Character.charCount(cp)
        }
        return hasEmojiCodePoint
    }

    fun filterEmojiOnly(text: String): String {
        val tokens = parseEmojiTokens(text)
        return tokens.filter { isEmojiToken(it) }.joinToString("")
    }

    fun filterSymbolsOnly(text: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < text.length) {
            val cp = text.codePointAt(i)
            val charCount = Character.charCount(cp)
            val token = text.substring(i, i + charCount)
            if (!Character.isLetterOrDigit(cp) && !isEmojiCodePoint(cp) && !isEmojiToken(token)) {
                sb.append(token)
            }
            i += charCount
        }
        return sb.toString()
    }

    fun calculateMaxPossibleUniqueCodes(poolSize: Int, length: Int, uniqueCharacters: Boolean): Double {
        if (poolSize <= 0 || length <= 0) return 0.0
        if (uniqueCharacters) {
            if (length > poolSize) return 0.0
            var count = 1.0
            for (i in 0 until length) {
                count *= (poolSize - i)
                if (count > 1e12) break
            }
            return count
        } else {
            return poolSize.toDouble().pow(length.toDouble())
        }
    }

    fun validate(config: GeneratorConfig): ValidationResult {
        if (config.quantity !in 1..100000) {
            return ValidationResult.Error("Quantity must be between 1 and 100,000.")
        }
        if (config.length !in 4..64 && !config.usePatternMask) {
            return ValidationResult.Error("Length must be between 4 and 64.")
        }

        if (config.usePatternMask) {
            if (config.patternMask.isBlank()) {
                return ValidationResult.Error("Pattern mask cannot be empty.")
            }
            return ValidationResult.Success
        }

        if (config.useCustomWhitelist && config.customWhitelist.isBlank()) {
            return ValidationResult.Error("Whitelist Character cannot be empty when enabled.")
        }

        val pool = buildTokenPool(config)
        if (pool.isEmpty()) {
            return ValidationResult.Error("Please select at least one character set (Lower, Upper, Numbers, Symbols, or Emojis).")
        }

        if (config.uniqueCharacters && config.length > pool.size) {
            return ValidationResult.Error(
                "Cannot generate unique codes: Requested length (${config.length}) exceeds available distinct tokens (${pool.size})."
            )
        }

        val maxPossible = calculateMaxPossibleUniqueCodes(pool.size, config.length, config.uniqueCharacters)
        if (maxPossible in 1.0..<config.quantity.toDouble()) {
            return ValidationResult.Error(
                "Requested quantity (${config.quantity}) exceeds total possible unique combinations (${maxPossible.toLong()})."
            )
        }

        val digitsPool = pool.filter { it.length == 1 && it[0] in '0'..'9' }
        val upperPool = pool.filter { it.length == 1 && it[0] in 'A'..'Z' }
        val symbolsPool = pool.filter { it.length == 1 && !it[0].isLetterOrDigit() && !isEmojiToken(it) }

        if (config.minDigits > 0) {
            if (config.uniqueCharacters && config.minDigits > digitsPool.size) {
                return ValidationResult.Error(
                    "Minimum digits constraint (${config.minDigits}) exceeds distinct digits available (${digitsPool.size})."
                )
            }
            if (!config.uniqueCharacters && digitsPool.isEmpty()) {
                return ValidationResult.Error(
                    "Minimum digits requested (${config.minDigits}), but no digits exist in the selected character sets."
                )
            }
        }

        if (config.minUpper > 0) {
            if (config.uniqueCharacters && config.minUpper > upperPool.size) {
                return ValidationResult.Error(
                    "Minimum upper-case constraint (${config.minUpper}) exceeds distinct upper-case letters available (${upperPool.size})."
                )
            }
            if (!config.uniqueCharacters && upperPool.isEmpty()) {
                return ValidationResult.Error(
                    "Minimum upper-case requested (${config.minUpper}), but no upper-case letters exist in the selected character sets."
                )
            }
        }

        if (config.minSymbols > 0) {
            if (config.uniqueCharacters && config.minSymbols > symbolsPool.size) {
                return ValidationResult.Error(
                    "Minimum symbols constraint (${config.minSymbols}) exceeds distinct symbols available (${symbolsPool.size})."
                )
            }
            if (!config.uniqueCharacters && symbolsPool.isEmpty()) {
                return ValidationResult.Error(
                    "Minimum symbols requested (${config.minSymbols}), but no symbols exist in the selected character sets."
                )
            }
        }

        val totalMinReq = config.minDigits + config.minUpper + config.minSymbols
        if (totalMinReq > config.length) {
            return ValidationResult.Error(
                "Minimum character constraints (Digits: ${config.minDigits}, Upper: ${config.minUpper}, Symbols: ${config.minSymbols}) exceed requested length (${config.length})."
            )
        }

        return ValidationResult.Success
    }

    fun buildTokenPool(config: GeneratorConfig): List<String> {
        val blacklistSet = HashSet<String>()
        if (config.useBlacklist && config.blacklistChars.isNotBlank()) {
            config.blacklistChars
                .replace(" ", "")
                .replace(",", "")
                .forEach { ch ->
                    blacklistSet.add(ch.toString())
                    blacklistSet.add(ch.lowercaseChar().toString())
                    blacklistSet.add(ch.uppercaseChar().toString())
                }
        }

        if (config.useCustomWhitelist && config.customWhitelist.isNotBlank()) {
            var tokens = parseEmojiTokens(config.customWhitelist).distinct()
            if (config.avoidAmbiguous) {
                tokens = tokens.filter { !(it.length == 1 && it[0] in AMBIGUOUS_CHARS) }
            }
            if (blacklistSet.isNotEmpty()) {
                tokens = tokens.filter { it !in blacklistSet }
            }
            return tokens
        }

        val tokens = ArrayList<String>()
        if (config.includeLower) {
            val lowerSource = if (config.customLower.isNotBlank()) config.customLower else "abcdefghijklmnopqrstuvwxyz"
            lowerSource.filter { it in 'a'..'z' }.forEach { tokens.add(it.toString()) }
        }
        if (config.includeUpper) {
            val upperSource = if (config.customUpper.isNotBlank()) config.customUpper else "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
            upperSource.filter { it in 'A'..'Z' }.forEach { tokens.add(it.toString()) }
        }
        if (config.includeNumbers) {
            val numSource = if (config.customNumbers.isNotBlank()) config.customNumbers else "0123456789"
            numSource.filter { it in '0'..'9' }.forEach { tokens.add(it.toString()) }
        }
        if (config.includeSymbols) {
            val symSource = if (config.customSymbols.isNotBlank()) config.customSymbols else "'\"!?.,:;\$%&@~#()<>{}[]_*-+^=/|\\"
            filterSymbolsOnly(symSource).forEach { tokens.add(it.toString()) }
        }
        if (config.includeEmoji) {
            val emojiSource = if (config.customEmoji.isNotBlank()) config.customEmoji else EMOJI_POOL.joinToString("")
            tokens.addAll(parseEmojiTokens(emojiSource).filter { isEmojiToken(it) })
        }

        var result = tokens.distinct()

        if (config.avoidAmbiguous) {
            result = result.filter { !(it.length == 1 && it[0] in AMBIGUOUS_CHARS) }
        }

        if (blacklistSet.isNotEmpty()) {
            result = result.filter { it !in blacklistSet }
        }

        return result
    }

    fun buildCharPool(config: GeneratorConfig): List<Char> {
        return buildTokenPool(config).flatMap { token -> token.toList() }
    }

    fun generate(config: GeneratorConfig): List<String> {
        val pool = buildTokenPool(config)
        val isSeeded = config.seed.isNotBlank()
        val seededRandom = if (isSeeded) Random(config.seed.trim().hashCode().toLong()) else null
        val secureRandom = if (!isSeeded) SecureRandom() else null

        fun nextInt(bound: Int): Int {
            if (bound <= 0) return 0
            return seededRandom?.nextInt(bound) ?: secureRandom!!.nextInt(bound)
        }

        fun randomFrom(list: List<String>): String {
            if (list.isEmpty()) return "X"
            return list[nextInt(list.size)]
        }

        val digitsPool = pool.filter { it.length == 1 && it[0] in '0'..'9' }
        val upperPool = pool.filter { it.length == 1 && it[0] in 'A'..'Z' }
        val symbolsPool = pool.filter { it.length == 1 && !it[0].isLetterOrDigit() && !isEmojiToken(it) }
        val lowerPool = pool.filter { it.length == 1 && it[0] in 'a'..'z' }

        val availableVowels = VOWELS.map { it.toString() }.filter { it in pool || pool.isEmpty() }
        val availableConsonants = CONSONANTS.map { it.toString() }.filter { it in pool || pool.isEmpty() }

        val results = ArrayList<String>(config.quantity)
        val seenCodes = HashSet<String>()
        val maxAttemptsPerCode = 300

        for (i in 0 until config.quantity) {
            var rawCode: String
            var attempts = 0

            do {
                attempts++
                if (config.usePatternMask) {
                    val sb = StringBuilder()
                    for (ch in config.patternMask) {
                        when (ch) {
                            '#' -> sb.append(if (digitsPool.isNotEmpty()) randomFrom(digitsPool) else nextInt(10).toString())
                            '@' -> sb.append(if (upperPool.isNotEmpty()) randomFrom(upperPool) else (65 + nextInt(26)).toChar().toString())
                            '$' -> sb.append(if (lowerPool.isNotEmpty()) randomFrom(lowerPool) else (97 + nextInt(26)).toChar().toString())
                            '*' -> sb.append(if (symbolsPool.isNotEmpty()) randomFrom(symbolsPool) else "!@#$%^&*"[nextInt(8)].toString())
                            else -> sb.append(ch)
                        }
                    }
                    rawCode = sb.toString()
                } else if (config.pronounceable) {
                    val sb = StringBuilder(config.length)
                    var useConsonant = nextInt(2) == 0
                    for (k in 0 until config.length) {
                        if (useConsonant && availableConsonants.isNotEmpty()) {
                            sb.append(randomFrom(availableConsonants))
                        } else if (!useConsonant && availableVowels.isNotEmpty()) {
                            sb.append(randomFrom(availableVowels))
                        } else {
                            sb.append(randomFrom(pool.ifEmpty { listOf("a") }))
                        }
                        useConsonant = !useConsonant
                    }
                    rawCode = sb.toString()
                } else {
                    val codeTokens = ArrayList<String>()

                    if (config.uniqueCharacters) {
                        val poolSet = LinkedHashSet(pool)

                        fun pickAndRemove(candidates: List<String>): String? {
                            val available = candidates.filter { it in poolSet }
                            if (available.isEmpty()) return null
                            val picked = available[nextInt(available.size)]
                            poolSet.remove(picked)
                            return picked
                        }

                        repeat(config.minDigits) {
                            pickAndRemove(digitsPool)?.let { codeTokens.add(it) }
                        }
                        repeat(config.minUpper) {
                            pickAndRemove(upperPool)?.let { codeTokens.add(it) }
                        }
                        repeat(config.minSymbols) {
                            pickAndRemove(symbolsPool)?.let { codeTokens.add(it) }
                        }

                        val remainingLen = config.length - codeTokens.size
                        for (k in 0 until remainingLen) {
                            val token = pickAndRemove(poolSet.toList()) ?: break
                            codeTokens.add(token)
                        }
                    } else {
                        repeat(config.minDigits) { if (digitsPool.isNotEmpty()) codeTokens.add(randomFrom(digitsPool)) }
                        repeat(config.minUpper) { if (upperPool.isNotEmpty()) codeTokens.add(randomFrom(upperPool)) }
                        repeat(config.minSymbols) { if (symbolsPool.isNotEmpty()) codeTokens.add(randomFrom(symbolsPool)) }

                        val remainingLen = config.length - codeTokens.size
                        for (k in 0 until remainingLen) {
                            if (pool.isNotEmpty()) {
                                codeTokens.add(randomFrom(pool))
                            }
                        }
                    }

                    if (config.shuffleCode) {
                        for (idx in codeTokens.indices.reversed()) {
                            val j = nextInt(idx + 1)
                            val temp = codeTokens[idx]
                            codeTokens[idx] = codeTokens[j]
                            codeTokens[j] = temp
                        }
                    }

                    rawCode = codeTokens.joinToString("")
                }

                when (config.casingMode) {
                    CasingMode.FORCE_UPPER -> rawCode = rawCode.uppercase()
                    CasingMode.FORCE_LOWER -> rawCode = rawCode.lowercase()
                    CasingMode.DEFAULT -> {}
                }

                var formattedCode = rawCode
                if (config.chunkSize > 0 && config.chunkSize < formattedCode.length && !config.usePatternMask) {
                    formattedCode = formattedCode.chunked(config.chunkSize).joinToString(config.delimiter)
                }

                if (config.prefix.isNotBlank()) {
                    formattedCode = "${config.prefix}$formattedCode"
                }
                if (config.suffix.isNotBlank()) {
                    formattedCode = "$formattedCode${config.suffix}"
                }

                if (config.appendChecksum) {
                    formattedCode = ChecksumHelper.appendChecksum(formattedCode)
                }

                rawCode = formattedCode

                val containsBadWords = config.enableProfanityFilter && ProfanityFilter.containsProfanity(rawCode)
                val isDuplicate = seenCodes.contains(rawCode)

                if (!isDuplicate && !containsBadWords) {
                    seenCodes.add(rawCode)
                    results.add(rawCode)
                    break
                }
            } while (attempts < maxAttemptsPerCode)

            if (attempts >= maxAttemptsPerCode) {
                break
            }
        }

        return results
    }
}
