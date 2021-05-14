package com.p2p.wallet.dashboard.repository

import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey

@Deprecated("this will be deleted")
interface WowletApiCallRepository {
    suspend fun getMinimumBalance(accountLenght: Long): Long
    suspend fun createAndInitializeTokenAccount(payer: Account, mintAddress: PublicKey, newAccount: Account): String
}