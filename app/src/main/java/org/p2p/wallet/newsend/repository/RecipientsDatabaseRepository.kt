package org.p2p.wallet.newsend.repository

import org.p2p.wallet.newsend.db.RecipientDao
import org.p2p.wallet.newsend.db.RecipientEntry
import org.p2p.wallet.send.model.AddressState
import org.p2p.wallet.send.model.SearchResult
import java.util.Date

private const val EMPTY_TIMESTAMP = 0L

class RecipientsDatabaseRepository(
    private val recipientDao: RecipientDao
) {

    suspend fun addRecipient(searchResult: SearchResult, date: Date) {
        recipientDao.insertOrReplace(searchResult.toRecipientEntry(date))
    }

    suspend fun getRecipients(): List<SearchResult> =
        recipientDao.getRecipients().map { it.toSearchResult() }

    suspend fun clear() {
        recipientDao.clearAll()
    }

    private fun RecipientEntry.toSearchResult(): SearchResult = if (nickname.isNullOrEmpty()) SearchResult.AddressOnly(
        addressState = AddressState(address),
        date = Date(dateTimestamp)
    ) else SearchResult.UsernameFound(
        addressState = AddressState(address),
        username = nickname,
        date = Date(dateTimestamp)
    )

    private fun SearchResult.toRecipientEntry(newDate: Date): RecipientEntry {
        val nickname: String?
        val dateTimestamp: Long
        when (this) {
            is SearchResult.UsernameFound -> {
                nickname = username
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
        return RecipientEntry(
            address = addressState.address,
            nickname = nickname,
            dateTimestamp = dateTimestamp
        )
    }
}
