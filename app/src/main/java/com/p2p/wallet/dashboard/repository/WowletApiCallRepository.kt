package com.p2p.wallet.dashboard.repository

import com.p2p.wallet.common.network.Result
import com.p2p.wallet.dashboard.model.local.BalanceInfo
import com.p2p.wallet.dashboard.model.local.SendTransactionModel
import com.p2p.wallet.dashboard.model.orderbook.OrderBooks
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.rpc.types.AccountInfo
import org.p2p.solanaj.rpc.types.QRAccountInfo
import org.p2p.solanaj.rpc.types.TransferInfoResponse

interface WowletApiCallRepository {
    suspend fun generatePhrase(): List<String>
    suspend fun sendTransaction(sendTransactionModel: SendTransactionModel): String
    suspend fun getWallets(publicKey: String): MutableList<BalanceInfo>
    suspend fun getAccountInfo(publicKey: PublicKey): AccountInfo
    suspend fun getQRAccountInfo(publicKey: PublicKey): QRAccountInfo
    suspend fun getOrderBooks(tokenSymbol: String): Result<OrderBooks>
    suspend fun getMinimumBalance(accountLenght: Long): Long
    suspend fun getDetailActivityData(publicKey: String): List<TransferInfoResponse>
    suspend fun getBalance(accountAddress: String): Long
    suspend fun getConfirmedTransaction(signature: String, slot: Long): TransferInfoResponse?
    suspend fun getBlockTime(slot: Long): Long
    suspend fun getFee(): Long
    suspend fun createAndInitializeTokenAccount(payer: Account, mintAddress: PublicKey, newAccount: Account): String
}