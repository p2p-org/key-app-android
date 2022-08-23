package org.p2p.wallet.restore.ui.keys.adapter

import org.p2p.wallet.restore.model.SecretKey
import timber.log.Timber

object SeedPhraseUtils {

    fun format(text: String): List<SecretKey> {
        val splitted = text.split(" ")
        return when {
            /* First we are checking if there are multiple keys entered */
            splitted.size > 1 && splitted.last().isNotEmpty() -> {
                splitted.map { SecretKey(it.trim()) }
            }
            /* User enters single key here. Checking if he has finished by entering space */
            text.isNotBlank() && text.endsWith(" ") -> {
                listOf(SecretKey(text.trim()))
            }
            else -> {
                Timber
                    .tag("SEED_PHRASE")
                    .d("User is typing the word and not finished yet, doing nothing $text")
                emptyList()
            }
        }
    }
}
