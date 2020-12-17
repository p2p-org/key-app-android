package com.wowlet.domain.usecases

import com.github.mikephil.charting.data.Entry
import com.wowlet.data.datastore.DetailActivityRepository
import com.wowlet.data.datastore.WowletApiCallRepository
import com.wowlet.domain.extentions.fromHistoricalPricesToChartItem
import com.wowlet.domain.extentions.transferInfoToActivityItem
import com.wowlet.domain.interactors.DetailActivityInteractor
import com.wowlet.domain.utils.secondToDate
import com.wowlet.entities.CallException
import com.wowlet.entities.Constants
import com.wowlet.entities.Result
import com.wowlet.entities.local.ActivityItem


class DetailActivityUseCase(
    private val wowletApiCallRepository: WowletApiCallRepository,
    private val detailActivityRepository: DetailActivityRepository
) : DetailActivityInteractor {

    override suspend fun getActivityList(
        publicKey: String,
        icon: String,
        tokenName: String
    ): List<ActivityItem> {
        val walletsList = wowletApiCallRepository.getDetailActivityData(publicKey).map {
            it.transferInfoToActivityItem(publicKey, icon, tokenName)
        }

        return walletsList
    }

    override suspend fun blockTime(slot: Long): Result<String> {
        return try {
            val time = wowletApiCallRepository.getBlockTime(slot)
            val stringDate = time.secondToDate()
            Result.Success(stringDate)
        } catch (e: Exception) {
            Result.Error(CallException(Constants.REQUEST_EXACTION, e.message))
        }
    }

    override suspend fun getChatListByDate(tokenSymbol: String, startTime: Long, endTime: Long): Result<List<Entry>> {
       return when (val data = detailActivityRepository.getHistoricalPricesByDate(tokenSymbol + "USDT", startTime, endTime)) {
            is Result.Success -> {
                val chartData = data.data?.mapIndexed { index, historicalPrices ->
                    historicalPrices.fromHistoricalPricesToChartItem(index)
                }
                Result.Success(chartData)
            }
            is Result.Error -> {
                Result.Error(CallException(Constants.REQUEST_EXACTION, "Error char data load"))
            }
        }
    }

    override suspend fun getChatList(tokenSymbol: String): Result<List<Entry>> {
       return when (val data = detailActivityRepository.getAllHistoricalPrices(tokenSymbol + "USDT")) {
            is Result.Success -> {
                val chartData = data.data?.mapIndexed { index, historicalPrices ->
                    historicalPrices.fromHistoricalPricesToChartItem(index)
                }
                Result.Success(chartData)
            }
            is Result.Error -> {
                Result.Error(CallException(Constants.REQUEST_EXACTION, "Error char data load"))
            }
        }
    }

}
