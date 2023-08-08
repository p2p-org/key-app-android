package org.p2p.wallet.newsend.model

import android.os.Parcelable
import java.util.Date
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.p2p.core.token.Token
import org.p2p.core.token.TokenMetadata
import org.p2p.wallet.utils.CUT_ADDRESS_SYMBOLS_COUNT
import org.p2p.wallet.utils.cutMiddle

private const val EMPTY_BALANCE = 0L

sealed class SearchResult(open val address: String) : Parcelable {

    val formattedAddress: String
        get() = address.cutMiddle(CUT_ADDRESS_SYMBOLS_COUNT)

    fun isInvalid(): Boolean =
        this is InvalidDirectAddress || this is OwnAddressError

    @Parcelize
    data class InvalidDirectAddress(
        override val address: String,
        val directToken: TokenMetadata
    ) : SearchResult(address)

    @Parcelize
    data class OwnAddressError(
        override val address: String,
        val directToken: TokenMetadata? = null
    ) : SearchResult(address)

    @Parcelize
    data class AddressFound(
        override val address: String,
        val sourceToken: Token.Active? = null,
        val date: Date? = null,
        val balance: Long = EMPTY_BALANCE,
        val networkType: NetworkType = NetworkType.SOLANA
    ) : SearchResult(address) {
        @IgnoredOnParcel
        val isEmptyBalance = balance == EMPTY_BALANCE

        fun copyWithBalance(balance: Long): AddressFound {
            return AddressFound(address, sourceToken, date, balance)
        }
    }

    @Parcelize
    data class UsernameFound constructor(
        override val address: String,
        val username: String,
        val formattedUsername: String,
        val date: Date? = null
    ) : SearchResult(address)
}

fun TemporaryAccount.toSearchResult(): SearchResult =
    SearchResult.AddressFound(address)

fun SearchResult.nicknameOrAddress(): String {
    return if (this is SearchResult.UsernameFound) formattedUsername
    else address.cutMiddle(CUT_ADDRESS_SYMBOLS_COUNT)
}
