package org.p2p.wallet.send.repository

import org.p2p.wallet.send.model.SearchResult
import java.util.Date

interface RecipientsLocalRepository {
    suspend fun addRecipient(searchResult: SearchResult, date: Date)
    suspend fun getRecipients(): List<SearchResult>
    suspend fun clear()
}
