package org.p2p.wallet.newsend.model

import java.util.Date
import org.p2p.wallet.newsend.db.RecipientEntity
import org.p2p.wallet.utils.UsernameFormatter

private const val EMPTY_TIMESTAMP = 0L

object RecipientConverter {

    fun fromDatabase(
        entity: RecipientEntity,
        usernameFormatter: UsernameFormatter
    ): SearchResult {
        return if (entity.username.isNullOrEmpty()) SearchResult.AddressFound(
            addressState = AddressState(entity.address),
            date = Date(entity.dateTimestamp),
            networkType = NetworkType.valueOf(entity.networkTypeName)
        ) else SearchResult.UsernameFound(
            addressState = AddressState(entity.address),
            username = entity.username,
            formattedUsername = usernameFormatter.format(entity.username),
            date = Date(entity.dateTimestamp)
        )
    }

    fun toDatabase(searchResult: SearchResult, newDate: Date): RecipientEntity {
        val address = searchResult.addressState.address
        val nickname: String?
        val dateTimestamp: Long
        val networkType: NetworkType
        when (searchResult) {
            is SearchResult.UsernameFound -> {
                nickname = searchResult.username
                dateTimestamp = newDate.time
                networkType = NetworkType.SOLANA
            }
            is SearchResult.AddressFound -> {
                nickname = null
                dateTimestamp = newDate.time
                networkType = searchResult.networkType
            }
            else -> {
                nickname = null
                dateTimestamp = EMPTY_TIMESTAMP
                networkType = NetworkType.SOLANA
            }
        }
        return RecipientEntity(
            address = address,
            username = nickname,
            dateTimestamp = dateTimestamp,
            networkTypeName = networkType.name
        )
    }
}
