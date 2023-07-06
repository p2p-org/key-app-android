package org.p2p.wallet.history.ui.detailsbottomsheet

import org.p2p.core.crypto.Base58String

sealed class TransactionDetailsWalletAddressType(val value: String) {
    class Username(username: String) : TransactionDetailsWalletAddressType(username)
    class Address(address: Base58String) : TransactionDetailsWalletAddressType(address.base58Value)
}
