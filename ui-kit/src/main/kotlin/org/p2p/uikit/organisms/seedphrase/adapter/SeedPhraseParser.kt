package org.p2p.uikit.organisms.seedphrase.adapter

import org.p2p.uikit.organisms.seedphrase.SeedPhraseWord

class SeedPhraseParser {

    fun parse(seedPhrase: String): List<SeedPhraseWord> {
        val splitText = seedPhrase.split(" ")

        val multipleWordsEntered = splitText.size > 1 && splitText.last().isNotEmpty()
        val oneWordEntered = seedPhrase.isNotBlank() && seedPhrase.endsWith(" ")
        return when {
            /* First we are checking if there are multiple keys entered */
            multipleWordsEntered -> {
                splitText
                    .filter { it.isNotBlank() }
                    .map { SeedPhraseWord(text = it.trim(), isValid = true) }
            }
            /* User enters single key here. Checking if he has finished by entering space */
            oneWordEntered -> {
                listOf(SeedPhraseWord(text = splitText.first(), isValid = true))
            }
            else -> {
                emptyList()
            }
        }
    }
}
