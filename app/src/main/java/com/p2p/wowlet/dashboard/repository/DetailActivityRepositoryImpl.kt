package com.p2p.wowlet.dashboard.repository

import android.graphics.Bitmap
import com.p2p.wowlet.dashboard.api.RetrofitService
import com.p2p.wowlet.common.network.Result
import com.p2p.wowlet.common.network.HistoricalPrices
import com.p2p.wowlet.common.network.ResponceDataBonfida
import com.p2p.wowlet.utils.analyzeResponseObject
import com.p2p.wowlet.utils.makeApiCall
import net.glxn.qrgen.android.QRCode
import retrofit2.Response

class DetailActivityRepositoryImpl(val allApiService: RetrofitService) : DetailActivityRepository {

    override suspend fun getHistoricalPricesByDate(
        symbols: String,
        startTime: Long,
        endTime: Long
    ): Result<List<HistoricalPrices>> =
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

    override suspend fun getAllHistoricalPrices(symbols: String): Result<List<HistoricalPrices>> =
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
        response: Response<ResponceDataBonfida<List<HistoricalPrices>>>
    ): Result<List<HistoricalPrices>> =
        analyzeResponseObject(response)
}