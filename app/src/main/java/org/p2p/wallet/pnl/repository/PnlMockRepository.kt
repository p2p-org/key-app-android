package org.p2p.wallet.pnl.repository

import java.math.BigDecimal
import java.math.RoundingMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.p2p.core.crypto.Base58String
import org.p2p.wallet.pnl.api.PnlDataTimeSpan
import org.p2p.wallet.pnl.models.PnlData
import org.p2p.wallet.pnl.models.PnlTokenData
import org.p2p.wallet.pnl.storage.PnlStorageContract

class PnlMockRepository(
    private val storage: PnlStorageContract,
) : PnlRepository {
    override suspend fun getPnlData(
        timeSpan: PnlDataTimeSpan,
        userWallet: Base58String,
        tokenMints: List<Base58String>
    ): PnlData = withContext(Dispatchers.IO) {
        storage.getOrCache(tokenMints) {
            getPnlDataInternal(tokenMints)
        }
    }

    private suspend fun getPnlDataInternal(
        tokenMints: List<Base58String>
    ): PnlData {
        delay((2..5).random() * 1000L)
        return PnlData(
            total = generate(),
            tokens = tokenMints.associateWith { generate() }
        )
    }

    private fun generate(): PnlTokenData {
        val amount = BigDecimal(Math.random() * 50).setScale(2, RoundingMode.DOWN).toPlainString()
        val randomSign = if ((0..1).random() == 0) "+" else "-"
        val result = randomSign + amount
        return PnlTokenData(result, result)
    }
}
