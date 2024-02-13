package org.p2p.wallet.send.repository

import org.p2p.core.crypto.Base58String

interface SendStorageContract {
    fun restoreFeePayerToken(): Base58String?
    fun saveFeePayerToken(tokenMint: Base58String)
    fun removeFeePayerToken()
}
