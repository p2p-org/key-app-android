package org.p2p.wallet.send.model

import org.p2p.wallet.home.model.Token

sealed class CheckAddressResult {
    data class NewAccountNeeded(val feePayerToken: Token.Active) : CheckAddressResult()
    object AccountExists : CheckAddressResult()
    object InvalidAddress : CheckAddressResult()
}
