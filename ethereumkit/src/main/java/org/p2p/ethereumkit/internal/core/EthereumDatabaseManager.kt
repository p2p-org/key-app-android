package org.p2p.ethereumkit.internal.core

import android.content.Context
import org.p2p.ethereumkit.internal.api.storage.ApiDatabase
import org.p2p.ethereumkit.internal.core.storage.Eip20Database
import org.p2p.ethereumkit.internal.core.storage.TransactionDatabase
import org.p2p.ethereumkit.internal.models.Chain
import org.p2p.ethereumkit.internal.spv.core.storage.SpvDatabase

internal object EthereumDatabaseManager {

    fun getEthereumApiDatabase(context: Context, walletId: String, chain: Chain): ApiDatabase {
        return ApiDatabase.getInstance(context, getDbNameApi(walletId, chain))
    }

    fun getEthereumSpvDatabase(context: Context, walletId: String, chain: Chain): SpvDatabase {
        return SpvDatabase.getInstance(context, getDbNameSpv(walletId, chain))
    }

    fun getTransactionDatabase(context: Context, walletId: String, chain: Chain): TransactionDatabase {
        return TransactionDatabase.getInstance(context, getDbNameTransactions(walletId, chain))
    }

    fun getErc20Database(context: Context, walletId: String, chain: Chain): Eip20Database {
        return Eip20Database.getInstance(context, getDbNameErc20Events(walletId, chain))
    }

    fun clear(context: Context, chain: Chain, walletId: String) {
        synchronized(this) {
            context.deleteDatabase(getDbNameApi(walletId, chain))
            context.deleteDatabase(getDbNameSpv(walletId, chain))
            context.deleteDatabase(getDbNameTransactions(walletId, chain))
            context.deleteDatabase(getDbNameErc20Events(walletId, chain))
        }
    }

    private fun getDbNameApi(walletId: String, chain: Chain): String {
        return getDbName(chain, walletId, "api")
    }

    private fun getDbNameSpv(walletId: String, chain: Chain): String {
        return getDbName(chain, walletId, "spv")
    }

    private fun getDbNameTransactions(walletId: String, chain: Chain): String {
        return getDbName(chain, walletId, "txs")
    }

    private fun getDbNameErc20Events(walletId: String, chain: Chain): String {
        return getDbName(chain, walletId, "erc20_events")
    }

    private fun getDbName(chain: Chain, walletId: String, suffix: String): String {
        return "Ethereum-${chain.id}-$walletId-$suffix"
    }
}
