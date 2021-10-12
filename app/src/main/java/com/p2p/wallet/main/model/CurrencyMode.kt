package com.p2p.wallet.main.model

import android.content.Context
import com.p2p.wallet.R

sealed class CurrencyMode {
    data class Own(val symbol: String) : CurrencyMode()
    object Usd : CurrencyMode()

    fun getSymbol(context: Context): String =
        if (this is Own) this.symbol else context.getString(R.string.common_usd)
}