package com.p2p.wallet.user.model

import com.p2p.wallet.amount.valueOrZero
import com.p2p.wallet.common.crypto.Base58Utils
import com.p2p.wallet.main.api.MultiPriceResponse
import com.p2p.wallet.main.api.SinglePriceResponse
import com.p2p.wallet.main.model.TokenPrice
import org.bitcoinj.core.Utils
import org.p2p.solanaj.rpc.types.ProgramAccount
import java.math.BigDecimal

object UserConverter {

    fun fromNetwork(response: ProgramAccount): TokenProgramAccount {
        val data = Base58Utils.decode(response.account.data)

        val mintData = ByteArray(32)
        System.arraycopy(data, 0, mintData, 0, 32)

        val mint = Base58Utils.encode(mintData)
        val total = Utils.readInt64(data, 32 + 32)

        return TokenProgramAccount(response.pubkey, total, mint)
    }

    fun fromNetwork(tokenSymbol: String, response: MultiPriceResponse): TokenPrice = when (tokenSymbol) {
        "SOL" -> TokenPrice(tokenSymbol, usdOrZero(response.SOL))
        "BTC" -> TokenPrice(tokenSymbol, usdOrZero(response.BTC))
        "SRM" -> TokenPrice(tokenSymbol, usdOrZero(response.SRM))
        "MSRM" -> TokenPrice(tokenSymbol, usdOrZero(response.MSRM))
        "ETH" -> TokenPrice(tokenSymbol, usdOrZero(response.ETH))
        "FTT" -> TokenPrice(tokenSymbol, usdOrZero(response.FTT))
        "YFI" -> TokenPrice(tokenSymbol, usdOrZero(response.YFI))
        "LINK" -> TokenPrice(tokenSymbol, usdOrZero(response.LINK))
        "XRP" -> TokenPrice(tokenSymbol, usdOrZero(response.XRP))
        "USDT" -> TokenPrice(tokenSymbol, usdOrZero(response.USDT))
        "USDC" -> TokenPrice(tokenSymbol, usdOrZero(response.USDC))
        "WUSDC" -> TokenPrice(tokenSymbol, usdOrZero(response.WUSDC))
        "SUSHI" -> TokenPrice(tokenSymbol, usdOrZero(response.SUSHI))
        "ALEPH" -> TokenPrice(tokenSymbol, usdOrZero(response.ALEPH))
        "SXP" -> TokenPrice(tokenSymbol, usdOrZero(response.SXP))
        "HGET" -> TokenPrice(tokenSymbol, usdOrZero(response.HGET))
        "CREAM" -> TokenPrice(tokenSymbol, usdOrZero(response.CREAM))
        "UBXT" -> TokenPrice(tokenSymbol, usdOrZero(response.UBXT))
        "HNT" -> TokenPrice(tokenSymbol, usdOrZero(response.HNT))
        "FRONT" -> TokenPrice(tokenSymbol, usdOrZero(response.FRONT))
        "AKRO" -> TokenPrice(tokenSymbol, usdOrZero(response.AKRO))
        "HXRO" -> TokenPrice(tokenSymbol, usdOrZero(response.HXRO))
        "UNI" -> TokenPrice(tokenSymbol, usdOrZero(response.UNI))
        "MATH" -> TokenPrice(tokenSymbol, usdOrZero(response.MATH))
        "TOMO" -> TokenPrice(tokenSymbol, usdOrZero(response.TOMO))
        "LUA" -> TokenPrice(tokenSymbol, usdOrZero(response.LUA))
        else -> throw IllegalStateException("Unknown token symbol: $tokenSymbol")
    }

    fun fromNetwork(tokenSymbol: String, response: SinglePriceResponse): TokenPrice = when (tokenSymbol) {
        "USD" -> TokenPrice(tokenSymbol, response.usdValue.valueOrZero())
        "SOL" -> TokenPrice(tokenSymbol, response.SOL.valueOrZero())
        "BTC" -> TokenPrice(tokenSymbol, response.BTC.valueOrZero())
        "SRM" -> TokenPrice(tokenSymbol, response.SRM.valueOrZero())
        "MSRM" -> TokenPrice(tokenSymbol, response.MSRM.valueOrZero())
        "ETH" -> TokenPrice(tokenSymbol, response.ETH.valueOrZero())
        "FTT" -> TokenPrice(tokenSymbol, response.FTT.valueOrZero())
        "YFI" -> TokenPrice(tokenSymbol, response.YFI.valueOrZero())
        "LINK" -> TokenPrice(tokenSymbol, response.LINK.valueOrZero())
        "XRP" -> TokenPrice(tokenSymbol, response.XRP.valueOrZero())
        "USDT" -> TokenPrice(tokenSymbol, response.USDT.valueOrZero())
        "USDC" -> TokenPrice(tokenSymbol, response.USDC.valueOrZero())
        "WUSDC" -> TokenPrice(tokenSymbol, response.WUSDC.valueOrZero())
        "SUSHI" -> TokenPrice(tokenSymbol, response.SUSHI.valueOrZero())
        "ALEPH" -> TokenPrice(tokenSymbol, response.ALEPH.valueOrZero())
        "SXP" -> TokenPrice(tokenSymbol, response.SXP.valueOrZero())
        "HGET" -> TokenPrice(tokenSymbol, response.HGET.valueOrZero())
        "CREAM" -> TokenPrice(tokenSymbol, response.CREAM.valueOrZero())
        "UBXT" -> TokenPrice(tokenSymbol, response.UBXT.valueOrZero())
        "HNT" -> TokenPrice(tokenSymbol, response.HNT.valueOrZero())
        "FRONT" -> TokenPrice(tokenSymbol, response.FRONT.valueOrZero())
        "AKRO" -> TokenPrice(tokenSymbol, response.AKRO.valueOrZero())
        "HXRO" -> TokenPrice(tokenSymbol, response.HXRO.valueOrZero())
        "UNI" -> TokenPrice(tokenSymbol, response.UNI.valueOrZero())
        "MATH" -> TokenPrice(tokenSymbol, response.MATH.valueOrZero())
        "TOMO" -> TokenPrice(tokenSymbol, response.TOMO.valueOrZero())
        "LUA" -> TokenPrice(tokenSymbol, response.LUA.valueOrZero())
        else -> throw IllegalStateException("Unknown token symbol: $tokenSymbol")
    }

    private fun usdOrZero(response: SinglePriceResponse?): BigDecimal = response?.usdValue.valueOrZero()
}