package com.wowlet.domain.usecases

import com.wowlet.data.datastore.PreferenceService
import com.wowlet.data.datastore.WowletApiCallRepository
import com.wowlet.domain.extentions.transferInfoToActivityItem
import com.wowlet.domain.interactors.SendCoinInteractor
import com.wowlet.entities.CallException
import com.wowlet.entities.Constants.Companion.ERROR_WALLET_ITEM_IS_NULL
import com.wowlet.entities.Constants.Companion.REQUEST_EXACTION
import com.wowlet.entities.local.SendTransactionModel
import java.lang.Exception
import com.wowlet.entities.Result
import com.wowlet.entities.local.ActivityItem
import com.wowlet.entities.local.WalletItem
import kotlinx.coroutines.delay
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.pow

class SendCoinUseCase(
    private val wowletApiCallRepository: WowletApiCallRepository,
    private val preferenceService: PreferenceService
) : SendCoinInteractor {

    override suspend fun sendCoin(coinData: SendTransactionModel): Result<ActivityItem> {
        val activeWallet = preferenceService.getActiveWallet()
        coinData.secretKey = activeWallet?.secretKey!!
       // coinData.secretKey = "4g8ivUf5LiczqSUs7v2XjhVetZKhSkEFFSRtD94sgtkG6jhNeKXZN6KHkbs2U6AJJWmBXQo8zqmNGFnbgA3F6VCu"
       coinData.fromPublicKey = activeWallet?.publicKey!!
      //  coinData.fromPublicKey = "22CbwPktYBVbTctjfsr35ozanxwfVjNbBofsnWY4C2YR"
        try {
            val signature = wowletApiCallRepository.sendTransaction(coinData)
            repeat(1000) {
                delay(120_000)
                val transaction = wowletApiCallRepository.getConfirmedTransaction(
                    signature,
                    0
                )?.transferInfoToActivityItem(coinData.fromPublicKey, "", "", "")
                transaction?.tokenSymbol=coinData.tokenSymbol
                return Result.Success(transaction)
            }
            return Result.Error(CallException(REQUEST_EXACTION, ""))
        } catch (e: Exception) {
            return Result.Error(CallException(REQUEST_EXACTION, e.message))
        }
    }

    override fun saveWalletItem(walletItem: WalletItem?) {
        preferenceService.setWalletItemData(walletItem)
    }

    override suspend fun getWalletItem(): Result<WalletItem> {
        val walletItemData = preferenceService.getWalletItemData()
        return if (walletItemData != null)
            Result.Success(walletItemData)
        else
            Result.Error(CallException(ERROR_WALLET_ITEM_IS_NULL))
    }

    override suspend fun getFee(): Result<BigDecimal> {
        return try {
            val fee = wowletApiCallRepository.getFee()
            val feeValue = (fee / 10.0.pow(9.0))
            val scale = BigDecimal(feeValue).setScale(6, RoundingMode.HALF_EVEN)
            Result.Success(scale)
        } catch (e: Exception) {
            Result.Error(CallException(REQUEST_EXACTION, e.message))
        }
    }


}