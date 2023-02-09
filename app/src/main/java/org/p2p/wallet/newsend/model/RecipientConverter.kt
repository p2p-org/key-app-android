package org.p2p.wallet.newsend.model

import org.p2p.wallet.newsend.db.RecipientEntity
import java.util.Date

private const val EMPTY_TIMESTAMP = 0L

object RecipientConverter {

    fun fromDatabase(
        entity: RecipientEntity
    ): SearchResult {
        return if (entity.username.isNullOrEmpty()) SearchResult.AddressFound(
            addressState = AddressState(entity.address),
            date = Date(entity.dateTimestamp)
        ) else SearchResult.UsernameFound(
            addressState = AddressState(entity.address),
            username = entity.username,
            date = Date(entity.dateTimestamp)
        )
    }

    fun toDatabase(searchResult: SearchResult, newDate: Date): RecipientEntity {
        val nickname: String?
        val dateTimestamp: Long
        when (searchResult) {
            is SearchResult.UsernameFound -> {
                nickname = searchResult.username
                dateTimestamp = newDate.time
            }
            is SearchResult.AddressFound -> {
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
            username = nickname,
            dateTimestamp = dateTimestamp
        )
    }
}
