package org.p2p.uikit.organisms.seedphrase

data class SeedPhraseWord(
    val text: String,
    val isValid: Boolean
) {
    companion object {
        val EMPTY_WORD = SeedPhraseWord(text = "", isValid = true)
    }
}
