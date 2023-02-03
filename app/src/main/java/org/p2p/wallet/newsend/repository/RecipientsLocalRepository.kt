package org.p2p.wallet.newsend.repository

import org.p2p.wallet.newsend.model.SearchResult
import java.util.Date

interface RecipientsLocalRepository {
    suspend fun addRecipient(searchResult: SearchResult, date: Date)
    suspend fun getRecipients(): List<SearchResult>
    suspend fun clear()
}
