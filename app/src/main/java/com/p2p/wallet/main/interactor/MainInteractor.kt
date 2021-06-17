package com.p2p.wallet.main.interactor

import com.p2p.wallet.R
import com.p2p.wallet.amount.toLamports
import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import com.p2p.wallet.main.model.TransactionResult
import com.p2p.wallet.main.repository.MainRepository
import com.p2p.wallet.token.model.Transaction
import org.p2p.solanaj.core.PublicKey
import java.math.BigDecimal

class MainInteractor(
    private val mainRepository: MainRepository,
    private val tokenKeyProvider: TokenKeyProvider
) {

    suspend fun sendToken(
        target: String,
        amount: BigDecimal,
        usdAmount: BigDecimal,
        decimals: Int,
        tokenSymbol: String
    ): TransactionResult {
        val currentUser = tokenKeyProvider.publicKey

        if (currentUser == target) {
            return TransactionResult.Error(R.string.main_send_to_yourself_error)
        }

        if (target.length < PublicKey.PUBLIC_KEY_LENGTH) {
            return TransactionResult.WrongWallet
        }

        val lamports = amount.toLamports(decimals)
        val signature = mainRepository.sendToken(target, lamports.toLong(), tokenSymbol)
        return TransactionResult.Success(signature, amount, usdAmount, tokenSymbol)
    }

    suspend fun getHistory(publicKey: String, tokenSymbol: String, limit: Int): List<Transaction> =
        mainRepository.getHistory(publicKey, tokenSymbol, limit)
}