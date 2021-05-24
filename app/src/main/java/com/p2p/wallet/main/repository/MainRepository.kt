package com.p2p.wallet.main.repository

import com.p2p.wallet.main.ui.transaction.TransactionInfo
import com.p2p.wallet.token.model.Transaction
import org.p2p.solanaj.kits.Pool
import org.p2p.solanaj.rpc.types.TokenAccountBalance
import java.math.BigDecimal

interface MainRepository {
    suspend fun sendToken(targetAddress: String, lamports: Long, tokenSymbol: String): String
    suspend fun getHistory(publicKey: String, tokenSymbol: String, limit: Int): List<Transaction>
}