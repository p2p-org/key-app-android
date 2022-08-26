package org.p2p.uikit.organisms.seedphrase.adapter

import org.p2p.uikit.organisms.seedphrase.SeedPhraseWord

object SeedPhraseFormatter {

    fun format(text: String): List<SeedPhraseWord> {
        val splitted = text.split(" ")
        return when {
            /* First we are checking if there are multiple keys entered */
            splitted.size > 1 && splitted.last().isNotEmpty() -> {
                splitted.map { SeedPhraseWord(it.trim()) }
            }
            /* User enters single key here. Checking if he has finished by entering space */
            text.isNotBlank() && text.endsWith(" ") -> {
                listOf(SeedPhraseWord(text.trim()))
            }
            else -> {
                emptyList()
            }
        }
    }
}
