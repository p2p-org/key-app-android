package com.wowlet.data.repository

import android.graphics.Bitmap
import com.wowlet.data.dataservice.RetrofitService
import com.wowlet.data.datastore.DetailActivityRepository
import com.wowlet.data.util.analyzeResponseObject
import com.wowlet.data.util.makeApiCall
import com.wowlet.entities.Result
import com.wowlet.entities.responce.HistoricalPrices
import com.wowlet.entities.responce.ResponceDataBonfida
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