package org.p2p.wallet.rpc.model

import androidx.annotation.StringRes
import org.p2p.wallet.swap.model.orca.TransactionAddressData

sealed class AddressValidation {
    data class Error(@StringRes val messageRes: Int) : AddressValidation()
    object WrongWallet : AddressValidation()
    data class Valid(val addressData: TransactionAddressData) : AddressValidation()
}