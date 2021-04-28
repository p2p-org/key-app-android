package com.p2p.wallet.main.interactor

import com.p2p.wallet.main.model.SendTokenResult
import com.p2p.wallet.main.repository.MainRepository

class MainInteractor(
    private val mainRepository: MainRepository
) {

    suspend fun sendToken(target: String, lamports: Long, tokenSymbol: String): SendTokenResult {
        val signature = mainRepository.sendToken(target, lamports, tokenSymbol)
        // todo: get confirmed transaction and set result

        return SendTokenResult.Success
    }
}