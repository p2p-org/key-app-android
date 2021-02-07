package com.wowlet.data.datastore

import com.wowlet.entities.Result
import com.wowlet.entities.local.BalanceInfo
import com.wowlet.entities.local.SendTransactionModel
import com.wowlet.entities.local.UserSecretData
import com.wowlet.entities.responce.orderbook.OrderBooks
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.rpc.types.AccountInfo
import org.p2p.solanaj.rpc.types.QRAccountInfo
import org.p2p.solanaj.rpc.types.TransferInfo


interface WowletApiCallRepository {
    suspend fun initAccount(phraseList: List<String>): UserSecretData
    fun generatePhrase(): List<String>
    suspend fun sendTransaction(sendTransactionModel: SendTransactionModel): String
    suspend fun getWallets(publicKey: String): MutableList<BalanceInfo>
    suspend fun getAccountInfo(publicKey: PublicKey): AccountInfo
    suspend fun getQRAccountInfo(publicKey: PublicKey): QRAccountInfo
    suspend fun getOrderBooks(tokenSymbol: String): Result<OrderBooks>
    suspend fun getMinimumBalance(accountLenght: Long): Long
    suspend fun getDetailActivityData(publicKey: String): List<TransferInfo>
    suspend fun getBalance(accountAddress: String): Long
    suspend fun getConfirmedTransaction(signature: String, slot: Long): TransferInfo?
    suspend fun getBlockTime(slot: Long): Long
    suspend fun getFee(): Long
    suspend fun createAndInitializeTokenAccount(payer: Account, mintAddress: PublicKey, newAccount: Account): String
}