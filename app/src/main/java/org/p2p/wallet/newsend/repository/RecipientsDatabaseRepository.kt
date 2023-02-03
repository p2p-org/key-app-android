package org.p2p.wallet.newsend.repository

import org.p2p.wallet.newsend.db.RecipientsDao
import org.p2p.wallet.newsend.model.RecipientConverter
import org.p2p.wallet.newsend.model.SearchResult
import java.util.Date

class RecipientsDatabaseRepository(
    private val recipientsDao: RecipientsDao
) : RecipientsLocalRepository {

    override suspend fun addRecipient(searchResult: SearchResult, date: Date) {
        recipientsDao.insertOrReplace(RecipientConverter.toDatabase(searchResult, date))
    }

    override suspend fun getRecipients(): List<SearchResult> =
        recipientsDao.getRecipients().map { RecipientConverter.fromDatabase(it) }

    override suspend fun clear() {
        recipientsDao.clearAll()
    }
}
