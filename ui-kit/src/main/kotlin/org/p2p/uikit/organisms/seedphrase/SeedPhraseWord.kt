package org.p2p.uikit.organisms.seedphrase

data class SeedPhraseWord(
    val text: String,
    val isValid: Boolean,
    var isBlurred: Boolean = false
) {
    companion object {
        val EMPTY_WORD = SeedPhraseWord(text = "", isValid = true, isBlurred = false)
    }
}
