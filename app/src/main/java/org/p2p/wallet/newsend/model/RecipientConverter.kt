package org.p2p.wallet.newsend.model

import org.p2p.core.token.Token
import org.p2p.core.token.TokenData
import org.p2p.core.token.TokenVisibility
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.wallet.home.db.TokenEntity
import org.p2p.wallet.newsend.db.RecipientEntity
import org.p2p.wallet.send.model.AddressState
import org.p2p.wallet.send.model.SearchResult
import java.util.Date

private const val EMPTY_TIMESTAMP = 0L

object RecipientConverter {

    fun fromDatabase(
        entity: RecipientEntity
    ): SearchResult = if (entity.nickname.isNullOrEmpty()) SearchResult.AddressOnly(
        addressState = AddressState(entity.address),
        date = Date(entity.dateTimestamp)
    ) else SearchResult.UsernameFound(
        addressState = AddressState(entity.address),
        username = entity.nickname,
        date = Date(entity.dateTimestamp)
    )

    fun toDatabase(searchResult: SearchResult, newDate: Date): RecipientEntity {
        val nickname: String?
        val dateTimestamp: Long
        when (searchResult) {
            is SearchResult.UsernameFound -> {
                nickname = searchResult.username
                dateTimestamp = newDate.time
            }
            is SearchResult.AddressOnly -> {
                nickname = null
                dateTimestamp = newDate.time
            }
            else -> {
                nickname = null
                dateTimestamp = EMPTY_TIMESTAMP
            }
        }
        return RecipientEntity(
            address = searchResult.addressState.address,
            nickname = nickname,
            dateTimestamp = dateTimestamp
        )
    }

    fun fromDatabase(entity: TokenEntity): Token.Active =
        Token.Active(
            publicKey = entity.publicKey,
            tokenSymbol = entity.tokenSymbol,
            decimals = entity.decimals,
            mintAddress = entity.mintAddress,
            tokenName = entity.tokenName,
            iconUrl = entity.iconUrl,
            totalInUsd = entity.totalInUsd,
            total = entity.total,
            rate = entity.exchangeRate?.toBigDecimalOrZero(),
            visibility = TokenVisibility.parse(entity.visibility),
            serumV3Usdc = entity.serumV3Usdc,
            serumV3Usdt = entity.serumV3Usdt,
            isWrapped = entity.isWrapped
        )

    fun toTokenData(token: Token): TokenData =
        TokenData(
            mintAddress = token.mintAddress,
            name = token.tokenName,
            symbol = token.tokenSymbol,
            iconUrl = token.iconUrl,
            decimals = token.decimals,
            isWrapped = token.isWrapped,
            serumV3Usdc = token.serumV3Usdc,
            serumV3Usdt = token.serumV3Usdt,
        )
}
