package org.p2p.uikit.organisms.seedphrase.adapter

import org.p2p.uikit.organisms.seedphrase.SeedPhraseWord

object SeedPhraseFormatter {

    fun format(text: String): List<SeedPhraseWord> {
        val splitText = text.split(" ")
        return when {
            /* First we are checking if there are multiple keys entered */
            splitText.size > 1 && splitText.last().isNotEmpty() -> {
                splitText.map { SeedPhraseWord(text = it.trim(), isValid = true) }
            }
            /* User enters single key here. Checking if he has finished by entering space */
            text.isNotBlank() && text.endsWith(" ") -> {
                listOf(SeedPhraseWord(text = text.trim(), isValid = true))
            }
            else -> {
                emptyList()
            }
        }
    }
}
