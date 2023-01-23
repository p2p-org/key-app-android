package org.p2p.wallet.infrastructure.sell

import androidx.core.content.edit
import android.content.SharedPreferences

class HiddenSellTransactionsStorage(
    private val sharedPreferences: SharedPreferences
) : HiddenSellTransactionsStorageContract {

    override fun putTransaction(transactionId: String) {
        sharedPreferences.edit { putBoolean(transactionId, true) }
    }

    override fun isTransactionHidden(transactionId: String): Boolean = sharedPreferences.contains(transactionId)
}
