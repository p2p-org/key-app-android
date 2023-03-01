package org.p2p.ethereumkit.spv.core.storage

import org.p2p.ethereumkit.core.ISpvStorage
import org.p2p.ethereumkit.spv.models.AccountStateSpv
import org.p2p.ethereumkit.spv.models.BlockHeader

class SpvStorage(private val database: SpvDatabase) : ISpvStorage {

    override fun getLastBlockHeader(): BlockHeader? {
        return database.blockHeaderDao().getAll().firstOrNull()
    }

    override fun saveBlockHeaders(blockHeaders: List<BlockHeader>) {
        return database.blockHeaderDao().insertAll(blockHeaders)
    }

    override fun getBlockHeadersReversed(fromBlockHeight: Long, limit: Int): List<BlockHeader> {
        return database.blockHeaderDao().getByBlockHeightRange(fromBlockHeight - limit, fromBlockHeight)
    }

    override fun getAccountState(): AccountStateSpv? {
        return database.accountStateDao().getAccountState()
    }

    override fun saveAccountSate(accountState: AccountStateSpv) {
        database.accountStateDao().insert(accountState)
    }

}
