package com.p2p.wowlet.repository

import android.graphics.Bitmap
import com.p2p.wowlet.dataservice.RetrofitService
import com.p2p.wowlet.datastore.DashboardRepository
import com.p2p.wowlet.entities.Result
import com.p2p.wowlet.entities.local.ConstWalletItem
import com.p2p.wowlet.entities.responce.HistoricalPrices
import com.p2p.wowlet.entities.responce.ResponceDataBonfida
import com.p2p.wowlet.utils.WalletDataConst
import com.p2p.wowlet.utils.analyzeResponseObject
import com.p2p.wowlet.utils.makeApiCall
import net.glxn.qrgen.android.QRCode
import retrofit2.Response

class DashboardRepositoryImpl(val allApiService: RetrofitService) : DashboardRepository {
    override fun getQrCode(publicKey: String): Bitmap {
        return QRCode.from(publicKey).bitmap()
    }

    override fun getConstWallets(): List<ConstWalletItem> = WalletDataConst.getWalletConstList()

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

    private fun getHistoricalPricesData(
        response: Response<ResponceDataBonfida<List<HistoricalPrices>>>
    ): Result<List<HistoricalPrices>> =
        analyzeResponseObject(response)
}