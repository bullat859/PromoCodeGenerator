package com.m.payra.promo.generator.app.engine

import java.util.Locale

object ProfanityFilter {

    private val BAD_WORDS = setOf(
        "DAMN", "HELL", "CRAP", "SHIT", "FUCK", "BITCH", "SLUT", "BASTARD",
        "DICK", "COCK", "PISS", "ASS", "CUNT", "KIKER", "NIGG", "FAG", "DYKE",
    )

    private val BENIGN_EXCEPTIONS = listOf(
        "HELLO", "SHELL", "PASS", "CLASS", "ASSET", "ASSIGN", "GLASS",
        "MASS", "BASS", "GRASS", "COMPASS", "ASSIST", "ASSERT", "ASSEMBLE",
        "ASSOCIATE", "EMBARRASS", "TISSUE", "DISMISS"
    )

    fun containsProfanity(code: String): Boolean {
        if (code.isBlank()) return false
        val clean = code.uppercase(Locale.US).replace(Regex("[^A-Z]"), "")
        if (clean.isEmpty()) return false

        var temp = clean
        for (exception in BENIGN_EXCEPTIONS) {
            temp = temp.replace(exception, "")
        }

        for (word in BAD_WORDS) {
            if (temp.contains(word)) return true
        }
        return false
    }
}
