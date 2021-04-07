package com.p2p.wowlet.domain.interactors

import com.p2p.wowlet.entities.Result

interface QrScannerInteractor {
    suspend fun getAccountInfo(publicKey: String): Result<String>
}