package org.p2p.uikit.organisms.seedphrase.adapter

import org.p2p.uikit.organisms.seedphrase.SeedPhraseKey

object SeedPhraseFormatter {

    fun format(text: String): List<SeedPhraseKey> {
        val splitted = text.split(" ")
        return when {
            /* First we are checking if there are multiple keys entered */
            splitted.size > 1 && splitted.last().isNotEmpty() -> {
                splitted.map { SeedPhraseKey(it.trim()) }
            }
            /* User enters single key here. Checking if he has finished by entering space */
            text.isNotBlank() && text.endsWith(" ") -> {
                listOf(SeedPhraseKey(text.trim()))
            }
            else -> {
                emptyList()
            }
        }
    }
}
