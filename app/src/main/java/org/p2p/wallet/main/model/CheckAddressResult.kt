package org.p2p.wallet.main.model

sealed class CheckAddressResult {
    data class NewAccountNeeded(val feePayerToken: Token.Active) : CheckAddressResult()
    object AccountExists : CheckAddressResult()
    object InvalidAddress : CheckAddressResult()
}