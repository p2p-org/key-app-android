package org.p2p.wallet.send.model

import org.p2p.wallet.feerelayer.model.RelayAccount
import org.p2p.wallet.home.model.Token

sealed class CheckAddressResult {
    data class NewAccountNeeded(val feePayerToken: Token.Active, val relayAccount: RelayAccount) : CheckAddressResult()
    object AccountExists : CheckAddressResult()
    object InvalidAddress : CheckAddressResult()
}