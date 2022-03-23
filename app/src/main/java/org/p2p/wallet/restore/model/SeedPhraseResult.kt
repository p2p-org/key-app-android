package org.p2p.wallet.restore.model

import androidx.annotation.StringRes

sealed class SeedPhraseResult {
    data class Success(val secretKeys: List<SecretKey>) : SeedPhraseResult()
    data class Error(@StringRes val message: Int) : SeedPhraseResult()
}
