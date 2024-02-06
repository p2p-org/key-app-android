package org.p2p.wallet.home.interactor

import org.p2p.core.token.Token

sealed interface CryptoSendOpenResult {
    object CanBeOpened : CryptoSendOpenResult
    class NoTokens(val fallbackBuyToken: Token?) : CryptoSendOpenResult
}
