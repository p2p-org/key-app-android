package org.p2p.wallet.send.model

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.p2p.core.token.Token
import org.p2p.core.token.TokenData
import org.p2p.wallet.utils.CUT_ADDRESS_SYMBOLS_COUNT
import org.p2p.wallet.utils.cutMiddle
import java.util.Date

private const val EMPTY_BALANCE = 0L
private const val USERNAME_KEY_APP_DOMAIN = ".key"

sealed class SearchResult(open val addressState: AddressState) : Parcelable {

    val formattedAddress: String
        get() = addressState.address.cutMiddle(CUT_ADDRESS_SYMBOLS_COUNT)

    fun isInvalid(): Boolean =
        this is InvalidDirectAddress || this is OwnAddressError

    @Parcelize
    data class InvalidDirectAddress(
        val address: String,
        val directToken: TokenData
    ) : SearchResult(AddressState(address))

    @Parcelize
    data class OwnAddressError(
        val address: String,
        val directToken: TokenData? = null
    ) : SearchResult(AddressState(address))

    @Parcelize
    data class AddressFound(
        override val addressState: AddressState,
        val sourceToken: Token.Active? = null,
        val date: Date? = null,
        val balance: Long = EMPTY_BALANCE
    ) : SearchResult(addressState) {
        @IgnoredOnParcel
        val isEmptyBalance = balance == EMPTY_BALANCE

        fun copyWithBalance(balance: Long): AddressFound {
            return AddressFound(addressState, sourceToken, date, balance)
        }
    }

    @Parcelize
    data class UsernameFound constructor(
        override val addressState: AddressState,
        val username: String,
        val date: Date? = null
    ) : SearchResult(addressState) {
        fun getFormattedUsername(): String = if (username.endsWith(USERNAME_KEY_APP_DOMAIN)) {
            "@$username"
        } else {
            username
        }
    }
}
