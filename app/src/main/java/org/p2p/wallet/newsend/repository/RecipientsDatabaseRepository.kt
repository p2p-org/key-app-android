package org.p2p.wallet.newsend.repository

import org.p2p.wallet.newsend.db.RecipientsDao
import org.p2p.wallet.newsend.model.RecipientConverter
import org.p2p.wallet.newsend.model.SearchResult
import java.util.Date
import org.p2p.wallet.utils.UsernameFormatter

class RecipientsDatabaseRepository(
    private val recipientsDao: RecipientsDao,
    private val usernameFormatter: UsernameFormatter
) : RecipientsLocalRepository {

    override suspend fun addRecipient(searchResult: SearchResult, date: Date) {
        recipientsDao.insertOrReplace(RecipientConverter.toDatabase(searchResult, date))
    }

    override suspend fun getRecipients(): List<SearchResult> =
        recipientsDao.getRecipients().map { RecipientConverter.fromDatabase(it, usernameFormatter) }

    override suspend fun clear() {
        recipientsDao.clearAll()
    }
}
