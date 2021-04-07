package com.p2p.wowlet.repository

import android.graphics.Bitmap
import com.p2p.wowlet.dataservice.RetrofitService
import com.p2p.wowlet.datastore.DetailActivityRepository
import com.p2p.wowlet.util.analyzeResponseObject
import com.p2p.wowlet.util.makeApiCall
import com.p2p.wowlet.entities.Result
import com.p2p.wowlet.entities.responce.HistoricalPrices
import com.p2p.wowlet.entities.responce.ResponceDataBonfida
import net.glxn.qrgen.android.QRCode
import retrofit2.Response


class DetailActivityRepositoryImpl(val allApiService: RetrofitService) : DetailActivityRepository {

    override suspend fun getHistoricalPricesByDate(symbols: String, startTime: Long, endTime: Long): Result<List<HistoricalPrices>> =
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

    private fun getHistoricalPricesData(response: Response<ResponceDataBonfida<List<HistoricalPrices>>>): Result<List<HistoricalPrices>> =
        analyzeResponseObject(response)

}