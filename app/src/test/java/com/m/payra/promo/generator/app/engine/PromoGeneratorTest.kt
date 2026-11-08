package com.m.payra.promo.generator.app.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PromoGeneratorTest {

    @Test
    fun testValidationEmptyPool() {
        val config = GeneratorConfig(
            includeLower = false,
            includeUpper = false,
            includeNumbers = false,
            includeSymbols = false,
            includeEmoji = false
        )
        val result = PromoGenerator.validate(config)
        assertTrue(result is ValidationResult.Error)
        assertEquals(
            "Please select at least one character set (Lower, Upper, Numbers, Symbols, or Emojis).",
            (result as ValidationResult.Error).message
        )
    }

    @Test
    fun testValidationUniqueCharsExceedLength() {
        val config = GeneratorConfig(
            includeLower = true,
            includeUpper = false,
            includeNumbers = false,
            includeSymbols = false,
            uniqueCharacters = true,
            length = 30
        )
        val result = PromoGenerator.validate(config)
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun testCaseInsensitiveLetterOmission() {
        val config = GeneratorConfig(
            includeLower = true,
            includeUpper = true,
            includeNumbers = false,
            useBlacklist = true,
            blacklistChars = "a, B"
        )
        val pool = PromoGenerator.buildCharPool(config)
        assertFalse(pool.contains('a'))
        assertFalse(pool.contains('A'))
        assertFalse(pool.contains('b'))
        assertFalse(pool.contains('B'))
    }

    @Test
    fun testSeededGenerationReproducibility() {
        val config1 = GeneratorConfig(
            quantity = 5,
            length = 10,
            seed = "PROMO_TEST_SEED_2026"
        )
        val config2 = GeneratorConfig(
            quantity = 5,
            length = 10,
            seed = "PROMO_TEST_SEED_2026"
        )

        val codes1 = PromoGenerator.generate(config1)
        val codes2 = PromoGenerator.generate(config2)

        assertEquals(5, codes1.size)
        assertEquals(codes1, codes2)
    }

    @Test
    fun testPrefixSuffixAndChunking() {
        val config = GeneratorConfig(
            quantity = 3,
            length = 8,
            prefix = "BF-",
            suffix = "-2026",
            chunkSize = 4,
            delimiter = "."
        )
        val codes = PromoGenerator.generate(config)
        assertEquals(3, codes.size)

        codes.forEach { code ->
            assertTrue(code.startsWith("BF-"))
            assertTrue(code.endsWith("-2026"))
            val inner = code.removePrefix("BF-").removeSuffix("-2026")
            assertTrue(inner.contains("."))
        }
    }

    @Test
    fun testPatternMaskGeneration() {
        val config = GeneratorConfig(
            quantity = 5,
            usePatternMask = true,
            patternMask = "SAVE-####-@@@@"
        )
        val codes = PromoGenerator.generate(config)
        assertEquals(5, codes.size)

        codes.forEach { code ->
            assertTrue(code.startsWith("SAVE-"))
            assertEquals(14, code.length)
            val parts = code.split("-")
            assertEquals("SAVE", parts[0])
            assertTrue(parts[1].all { it.isDigit() })
            assertTrue(parts[2].all { it.isUpperCase() })
        }
    }

    @Test
    fun testLuhnMod36Checksum() {
        val baseCode = "SUMMER2026"
        val withChecksum = ChecksumHelper.appendChecksum(baseCode)
        assertEquals(baseCode.length + 1, withChecksum.length)
        assertTrue(ChecksumHelper.verifyChecksum(withChecksum))
        assertFalse(ChecksumHelper.verifyChecksum("SUMMER2026X"))
    }

    @Test
    fun testProfanityFilter() {
        assertTrue(ProfanityFilter.containsProfanity("SOMEFUCKCODE"))
        assertTrue(ProfanityFilter.containsProfanity("test-shit-123"))
        assertFalse(ProfanityFilter.containsProfanity("SUMMER2026"))
        assertFalse(ProfanityFilter.containsProfanity("HELLO2026"))
        assertFalse(ProfanityFilter.containsProfanity("PASS2026"))
        assertFalse(ProfanityFilter.containsProfanity("SHELL2026"))
    }

    @Test
    fun testUniqueCharactersNoDuplicates() {
        val config = GeneratorConfig(
            quantity = 10,
            length = 8,
            includeLower = true,
            includeUpper = true,
            includeNumbers = true,
            uniqueCharacters = true,
            minDigits = 3
        )
        val codes = PromoGenerator.generate(config)
        assertEquals(10, codes.size)
        codes.forEach { code ->
            assertEquals(8, code.length)
            assertEquals(code.length, code.toSet().size)
        }
    }

    @Test
    fun testQuantityExceedsPossibleCombinationsValidation() {
        val config = GeneratorConfig(
            includeLower = false,
            includeUpper = false,
            includeNumbers = true,
            includeSymbols = false,
            quantity = 20,
            length = 1,
            uniqueCharacters = true
        )
        val result = PromoGenerator.validate(config)
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun testChecksumHelperWithFormattedCode() {
        val baseCode = "SUMMER2026"
        val withChecksum = ChecksumHelper.appendChecksum(baseCode)
        val formatted = withChecksum.chunked(4).joinToString("-")
        assertTrue(ChecksumHelper.verifyChecksum(formatted))
    }

    @Test
    fun testPronounceableMode() {
        val config = GeneratorConfig(
            quantity = 5,
            length = 8,
            pronounceable = true
        )
        val codes = PromoGenerator.generate(config)
        assertEquals(5, codes.size)
        codes.forEach { code ->
            assertEquals(8, code.length)
        }
    }

    @Test
    fun testPresetManager() {
        val presets = PresetManager.BUILT_IN_PRESETS
        assertTrue(presets.isNotEmpty())
        val bfPreset = presets.first { it.id == "black_friday" }
        assertEquals("BF2026-", bfPreset.config.prefix)
        assertEquals(30, bfPreset.config.expiryDays)
    }

    @Test
    fun testUniqueCharactersWithMinConstraints() {
        val config = GeneratorConfig(
            quantity = 5,
            length = 10,
            includeLower = true,
            includeUpper = true,
            includeNumbers = true,
            includeSymbols = true,
            uniqueCharacters = true,
            minDigits = 2,
            minUpper = 2,
            minSymbols = 1
        )
        val codes = PromoGenerator.generate(config)
        assertEquals(5, codes.size)
        codes.forEach { code ->
            assertEquals(10, code.length)
            assertEquals(code.length, code.toSet().size)
        }
    }

    @Test
    fun testChecksumWithPrefixAndSuffix() {
        val config = GeneratorConfig(
            quantity = 3,
            length = 8,
            prefix = "SUMMER-",
            suffix = "-2026",
            appendChecksum = true
        )
        val codes = PromoGenerator.generate(config)
        assertEquals(3, codes.size)
        codes.forEach { code ->
            assertTrue(ChecksumHelper.verifyChecksum(code))
        }
    }

    @Test
    fun testValidationMinConstraintsExceedPool() {
        val config = GeneratorConfig(
            includeLower = false,
            includeUpper = false,
            includeNumbers = true,
            includeSymbols = false,
            minUpper = 3
        )
        val result = PromoGenerator.validate(config)
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun testEmojiGeneration() {
        val config = GeneratorConfig(
            includeLower = false,
            includeUpper = false,
            includeNumbers = false,
            includeSymbols = false,
            includeEmoji = true,
            quantity = 5,
            length = 4
        )
        val validation = PromoGenerator.validate(config)
        assertTrue(validation is ValidationResult.Success)

        val codes = PromoGenerator.generate(config)
        assertEquals(5, codes.size)
        codes.forEach { code ->
            assertTrue(PromoGenerator.EMOJI_POOL.any { emoji -> code.contains(emoji) })
        }
    }

    @Test
    fun testCustomSymbolsFiltering() {
        val input = "ABC!@#123$%^xyz🎉🔥"
        val filtered = PromoGenerator.filterSymbolsOnly(input)
        assertEquals("!@#$%^", filtered)
    }

    @Test
    fun testEmojiOnlyFiltering() {
        val input = "abcABC123!@#🎉🔥🎁"
        val filtered = PromoGenerator.filterEmojiOnly(input)
        assertEquals("🎉🔥🎁", filtered)
    }

    @Test
    fun testLowercaseOnlyFiltering() {
        val input = "abcABC123!@#🎉🔥🎁"
        val filtered = input.filter { it in 'a'..'z' }
        assertEquals("abc", filtered)
    }

    @Test
    fun testUppercaseOnlyFiltering() {
        val input = "abcABC123!@#🎉🔥🎁"
        val filtered = input.filter { it in 'A'..'Z' }
        assertEquals("ABC", filtered)
    }

    @Test
    fun testDigitsOnlyFiltering() {
        val input = "abcABC123!@#🎉🔥🎁"
        val filtered = input.filter { it in '0'..'9' }
        assertEquals("123", filtered)
    }

    @Test
    fun testCharacterPoolIsolationInBuildTokenPool() {
        val config = GeneratorConfig(
            includeLower = true,
            customLower = "abcABC123!@#🎉",
            includeUpper = true,
            customUpper = "abcABC123!@#🎉",
            includeNumbers = true,
            customNumbers = "abcABC123!@#🎉",
            includeSymbols = true,
            customSymbols = "abcABC123!@#🎉",
            includeEmoji = true,
            customEmoji = "abcABC123!@#🎉"
        )
        val pool = PromoGenerator.buildTokenPool(config)
        // Check that pool tokens are strictly isolated
        assertTrue(pool.contains("a"))
        assertTrue(pool.contains("b"))
        assertTrue(pool.contains("c"))
        assertTrue(pool.contains("A"))
        assertTrue(pool.contains("B"))
        assertTrue(pool.contains("C"))
        assertTrue(pool.contains("1"))
        assertTrue(pool.contains("2"))
        assertTrue(pool.contains("3"))
        assertTrue(pool.contains("!"))
        assertTrue(pool.contains("@"))
        assertTrue(pool.contains("#"))
        assertTrue(pool.contains("🎉"))
        
        val digitsPool = pool.filter { it.length == 1 && it[0] in '0'..'9' }
        assertEquals(listOf("1", "2", "3"), digitsPool)

        val upperPool = pool.filter { it.length == 1 && it[0] in 'A'..'Z' }
        assertEquals(listOf("A", "B", "C"), upperPool)

        val lowerPool = pool.filter { it.length == 1 && it[0] in 'a'..'z' }
        assertEquals(listOf("a", "b", "c"), lowerPool)

        val symbolsPool = pool.filter { it.length == 1 && !it[0].isLetterOrDigit() && !PromoGenerator.isEmojiToken(it) }
        assertEquals(listOf("!", "@", "#"), symbolsPool)

        val emojiPool = pool.filter { PromoGenerator.isEmojiToken(it) }
        assertEquals(listOf("🎉"), emojiPool)
    }

    @Test
    fun testWhitelistAndBlacklistMutualExclusion() {
        val whitelist = "ABCDEF12345"
        val blacklistInput = "12345XYZ"
        val whitelistSet = whitelist.toSet()
        val filteredBlacklist = blacklistInput.filter { it !in whitelistSet }
        assertEquals("XYZ", filteredBlacklist)
    }

    @Test
    fun testShuffleCodeDisabled() {
        val config = GeneratorConfig(
            includeLower = false,
            includeUpper = true,
            customUpper = "ABC",
            includeNumbers = true,
            customNumbers = "123",
            minDigits = 3,
            minUpper = 3,
            length = 6,
            quantity = 5,
            shuffleCode = false
        )
        val codes = PromoGenerator.generate(config)
        assertEquals(5, codes.size)
        codes.forEach { code ->
            assertEquals(6, code.length)
            // First 3 chars must be digits
            assertTrue(code.substring(0, 3).all { it.isDigit() })
            // Next 3 chars must be uppercase letters
            assertTrue(code.substring(3, 6).all { it.isUpperCase() })
        }
    }
}
