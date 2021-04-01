package com.wowlet.data.repository

import android.graphics.Bitmap
import com.wowlet.data.dataservice.RetrofitService
import com.wowlet.data.datastore.DashboardRepository
import com.wowlet.data.util.WalletDataConst
import com.wowlet.data.util.analyzeResponseObject
import com.wowlet.data.util.makeApiCall
import com.wowlet.entities.Result
import com.wowlet.entities.local.ConstWalletItem
import com.wowlet.entities.responce.HistoricalPrices
import com.wowlet.entities.responce.ResponceDataBonfida
import net.glxn.qrgen.android.QRCode
import retrofit2.Response


class DashboardRepositoryImpl(val allApiService: RetrofitService) : DashboardRepository {
    override fun getQrCode(publicKey: String): Bitmap {
        return QRCode.from(publicKey).bitmap()
    }

    override fun getConstWallets(): List<ConstWalletItem> = WalletDataConst.walletConstList

    override suspend fun getHistoricalPrices(symbols: String): Result<List<HistoricalPrices>> =
        makeApiCall({
            getHistoricalPricesData(
                allApiService.getHistoricalPrices(
                    symbols,
                    1,
                    86400
                )
            )
        })

    private fun getHistoricalPricesData(response: Response<ResponceDataBonfida<List<HistoricalPrices>>>): Result<List<HistoricalPrices>> =
        analyzeResponseObject(response)

}