package org.p2p.wallet.main.model

import org.p2p.wallet.feerelayer.model.RelayAccount

sealed class CheckAddressResult {
    data class NewAccountNeeded(val feePayerToken: Token.Active, val relayAccount: RelayAccount) : CheckAddressResult()
    object AccountExists : CheckAddressResult()
    object InvalidAddress : CheckAddressResult()
}