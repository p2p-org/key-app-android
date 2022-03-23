package org.p2p.wallet.rpc.model

import androidx.annotation.StringRes

sealed class AddressValidation {
    data class Error(@StringRes val messageRes: Int) : AddressValidation()
    object WrongWallet : AddressValidation()
    object Valid : AddressValidation()
}
