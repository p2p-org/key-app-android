package org.p2p.wallet.send.model

sealed class CheckAddressResult {
    object NewAccountNeeded : CheckAddressResult()
    object AccountExists : CheckAddressResult()
    object InvalidAddress : CheckAddressResult()
}
