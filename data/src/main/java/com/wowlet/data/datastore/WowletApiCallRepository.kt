package com.wowlet.data.datastore

import com.wowlet.entities.responce.CallRequest
import com.wowlet.entities.Result
import com.wowlet.entities.local.SendTransactionModel
import com.wowlet.entities.local.UserSecretData
import com.wowlet.entities.responce.ResponseData
import com.wowlet.entities.responce.ResponseDataAirDrop


interface WowletApiCallRepository {
    suspend fun getBalance(requestModel: CallRequest): Result<ResponseData>
    suspend fun requestAirdrop(requestModel:CallRequest): Result<ResponseDataAirDrop>
    suspend fun initAccount(): UserSecretData
    suspend fun sendTransaction(sendTransactionModel: SendTransactionModel): String
}