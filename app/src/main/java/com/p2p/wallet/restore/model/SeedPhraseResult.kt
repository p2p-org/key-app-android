package com.p2p.wallet.restore.model

import androidx.annotation.StringRes

sealed class SeedPhraseResult {
    object Success : SeedPhraseResult()
    data class Error(@StringRes val message: Int) : SeedPhraseResult()
}