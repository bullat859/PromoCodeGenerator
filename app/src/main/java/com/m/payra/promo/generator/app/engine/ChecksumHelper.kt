package com.m.payra.promo.generator.app.engine

object ChecksumHelper {

    private const val ALPHANUMERIC = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"

    fun computeLuhnMod36(code: String): Char {
        val clean = code.uppercase().filter { it in ALPHANUMERIC }
        if (clean.isEmpty()) return '0'

        var factor = 2
        var sum = 0
        val n = ALPHANUMERIC.length

        for (i in clean.length - 1 downTo 0) {
            val codePoint = ALPHANUMERIC.indexOf(clean[i])
            if (codePoint < 0) continue

            var addend = factor * codePoint
            factor = if (factor == 2) 1 else 2

            addend = (addend / n) + (addend % n)
            sum += addend
        }

        val remainder = sum % n
        val checkCodePoint = (n - remainder) % n
        return ALPHANUMERIC[checkCodePoint]
    }

    fun appendChecksum(code: String): String {
        return code + computeLuhnMod36(code)
    }

    fun verifyChecksum(code: String): Boolean {
        val clean = code.uppercase().filter { it in ALPHANUMERIC }
        if (clean.length < 2) return false
        val base = clean.substring(0, clean.length - 1)
        val expected = computeLuhnMod36(base)
        return clean.last() == expected
    }
}
