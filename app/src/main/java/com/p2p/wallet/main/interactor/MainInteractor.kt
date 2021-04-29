package com.p2p.wallet.main.interactor

import com.p2p.wallet.R
import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import com.p2p.wallet.main.model.TransactionResult
import com.p2p.wallet.main.repository.MainRepository
import org.p2p.solanaj.core.PublicKey
import java.math.BigDecimal
import kotlin.math.pow

private const val VALUE_TO_CONVERT = 10.0

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

        val lamports = amount.toDouble() * VALUE_TO_CONVERT.pow(decimals)
        val signature = mainRepository.sendToken(target, lamports.toLong(), tokenSymbol)
        return TransactionResult.Success(signature, amount, usdAmount, tokenSymbol)
    }
}