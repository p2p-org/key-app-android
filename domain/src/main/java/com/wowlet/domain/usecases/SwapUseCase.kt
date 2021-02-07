package com.wowlet.domain.usecases

import com.wowlet.domain.interactors.SwapInteractor

class SwapUseCase : SwapInteractor {

    override fun getAroundToCurrencyValue(
        amount: String,
        walletBinds: Double,
        isInCryptoCurrency: Boolean
    ): Double  {
        val amountAsDouble: Double = if (amount == "" || amount == ".") 0.0 else amount.toDouble()
        return if (isInCryptoCurrency) {
            amountAsDouble.times(walletBinds)
        }else {
            amountAsDouble.div(walletBinds)
        }
    }

    override fun getAmountInConvertingToken(amount: String, from: Double, to: Double): Double {
        // 1 <from token> = currencyInFrom <to token>
        val currencyInFrom: Double = from.div(to)
        val amountAsDouble: Double = if (amount == "" || amount == ".") 0.0 else amount.toDouble()
        return amountAsDouble.times(currencyInFrom)
    }

    override fun getTokenPerToken(from: Double, to: Double): Double {
        return to.div(from)
    }

}