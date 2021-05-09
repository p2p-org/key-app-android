package com.p2p.wallet.dashboard.repository

import android.graphics.Bitmap
import com.p2p.wallet.dashboard.api.RetrofitService
import com.p2p.wallet.common.network.Result
import com.p2p.wallet.main.api.PriceHistoryResponse
import com.p2p.wallet.common.network.ResponceDataBonfida
import com.p2p.wallet.utils.analyzeResponseObject
import com.p2p.wallet.utils.makeApiCall
import net.glxn.qrgen.android.QRCode
import retrofit2.Response

class DetailActivityRepositoryImpl(val allApiService: RetrofitService) : DetailActivityRepository {

    override suspend fun getHistoricalPricesByDate(
        symbols: String,
        startTime: Long,
        endTime: Long
    ): Result<List<PriceHistoryResponse>> =
        makeApiCall({
            getHistoricalPricesData(
                allApiService.getHistoricalPrices(
                    symbols,
                    3600,
                    startTime,
                    endTime
                )
            )
        })

    override suspend fun getAllHistoricalPrices(symbols: String): Result<List<PriceHistoryResponse>> =
        makeApiCall({
            getHistoricalPricesData(
                allApiService.getAllHistoricalPrices(
                    symbols,
                    3600
                )
            )
        })

    override fun getQrCode(publicKey: String): Bitmap = QRCode.from(publicKey).bitmap()

    private fun getHistoricalPricesData(
        response: Response<ResponceDataBonfida<List<PriceHistoryResponse>>>
    ): Result<List<PriceHistoryResponse>> =
        analyzeResponseObject(response)
}