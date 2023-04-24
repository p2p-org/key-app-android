package org.p2p.wallet.newsend.model

import android.os.Parcelable
import java.util.Date
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.p2p.core.token.Token
import org.p2p.core.token.TokenData
import org.p2p.core.utils.formatUsername
import org.p2p.wallet.utils.CUT_ADDRESS_SYMBOLS_COUNT
import org.p2p.wallet.utils.cutMiddle

private const val EMPTY_BALANCE = 0L

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
        val balance: Long = EMPTY_BALANCE,
        val networkType: NetworkType = NetworkType.SOLANA
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
        fun getFormattedUsername(): String = formatUsername(username)
    }
}

fun TemporaryAccount.toSearchResult(): SearchResult =
    SearchResult.AddressFound(AddressState(address))
