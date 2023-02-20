package org.p2p.wallet.restore.model

import org.p2p.uikit.organisms.seedphrase.SeedPhraseWord

sealed interface SeedPhraseVerifyResult {
    class Verified(val seedPhrase: List<SeedPhraseWord>) : SeedPhraseVerifyResult {
        fun getKeys(): List<String> = seedPhrase.map { it.text }
    }

    class Invalid(val seedPhraseWord: List<SeedPhraseWord>) : SeedPhraseVerifyResult
    object VerificationFailed : SeedPhraseVerifyResult
}
