package com.p2p.wallet.dashboard.repository

import android.graphics.Bitmap
import com.p2p.wallet.dashboard.api.RetrofitService
import com.p2p.wallet.common.network.Result
import com.p2p.wallet.dashboard.model.local.ConstWallet
import com.p2p.wallet.main.api.PriceHistoryResponse
import com.p2p.wallet.common.network.ResponceDataBonfida
import com.p2p.wallet.utils.WalletDataConst
import com.p2p.wallet.utils.analyzeResponseObject
import com.p2p.wallet.utils.makeApiCall
import net.glxn.qrgen.android.QRCode
import retrofit2.Response

class DashboardRepositoryImpl(
    private val api: RetrofitService
) : DashboardRepository {
    override fun getQrCode(publicKey: String): Bitmap {
        return QRCode.from(publicKey).bitmap()
    }

    override fun getConstWallets(): List<ConstWallet> = WalletDataConst.getWalletConstList()

    override suspend fun getHistoricalPrices(symbols: String): Result<List<PriceHistoryResponse>> =
        makeApiCall({
            getHistoricalPricesData(
                api.getHistoricalPrices(
                    symbols,
                    1,
                    86400
                )
            )
        })

    private fun getHistoricalPricesData(
        response: Response<ResponceDataBonfida<List<PriceHistoryResponse>>>
    ): Result<List<PriceHistoryResponse>> =
        analyzeResponseObject(response)
}