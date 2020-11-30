package com.wowlet.data.datastore

import com.wowlet.entities.Result
import com.wowlet.entities.local.BalanceInfo
import com.wowlet.entities.local.SendTransactionModel
import com.wowlet.entities.local.UserSecretData
import com.wowlet.entities.responce.orderbook.OrderBooks
import org.p2p.solanaj.rpc.types.TransferInfo


interface WowletApiCallRepository {
    suspend fun initAccount(phraseList:List<String>): UserSecretData
    fun generatePhrase(): List<String>
    suspend fun sendTransaction(sendTransactionModel: SendTransactionModel): String
    suspend fun getWallets(publicKey: String): List<BalanceInfo>
    suspend fun getOrderBooks(tokenSymbol: String): Result<OrderBooks>
    suspend fun getMinimumBalance(accountLenght: Int): Int
    suspend fun getDetailActivityData(publicKey: String): List<TransferInfo>
}