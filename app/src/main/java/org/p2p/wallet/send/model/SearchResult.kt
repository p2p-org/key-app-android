package org.p2p.wallet.send.model

import android.os.Parcelable
import org.p2p.core.token.Token
import org.p2p.core.token.TokenData
import org.p2p.wallet.utils.CUT_ADDRESS_SYMBOLS_COUNT
import org.p2p.wallet.utils.cutMiddle
import java.util.Date
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

private const val EMPTY_BALANCE = 0L

sealed class SearchResult(open val addressState: AddressState) : Parcelable {

    val formattedAddress: String
        get() = addressState.address.cutMiddle(CUT_ADDRESS_SYMBOLS_COUNT)

    @Parcelize
    data class InvalidAddress(override val addressState: AddressState) : SearchResult(addressState)

    @Parcelize
    data class AddressOnly constructor(
        override val addressState: AddressState,
        val sourceToken: Token.Active? = null,
        val date: Date? = null,
        val balance: Long = EMPTY_BALANCE
    ) : SearchResult(addressState) {
        @IgnoredOnParcel
        val isEmptyBalance = balance == EMPTY_BALANCE

        fun copyWithBalance(balance: Long): AddressOnly {
            return AddressOnly(addressState, sourceToken, date, balance)
        }
    }

    @Parcelize
    data class UsernameFound(
        override val addressState: AddressState,
        val username: String,
        val date: Date? = null
    ) : SearchResult(addressState) {
        fun getFormattedUsername(): String = "@$username"
    }

    @Parcelize
    data class InvalidResult(
        override val addressState: AddressState,
        val errorMessage: String,
        val tokenData: TokenData? = null,
        val description: String? = null,
        val canReceiveAndBuy: Boolean = false
    ) : SearchResult(addressState)
}
