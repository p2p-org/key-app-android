package com.p2p.wallet.swap.interactor

import com.p2p.wallet.user.interactor.UserInteractor
import com.p2p.wallet.utils.toPublicKey
import org.p2p.solanaj.model.core.PublicKey
import org.p2p.solanaj.serumswap.instructions.SerumSwapInstructions.usdcMint
import org.p2p.solanaj.serumswap.instructions.SerumSwapInstructions.usdtMint

class SwapMarketInteractor(
    private val userInteractor: UserInteractor
) {

    // / Returns the `usdxMint` quoted market address.
    suspend fun getMarketAddress(
        usdxMint: PublicKey,
        baseMint: PublicKey
    ): PublicKey? {

        val tokens = userInteractor.getUserTokens()

        val token = tokens.firstOrNull {
            if (it.mintAddress != baseMint.toBase58()) return@firstOrNull false
            if (usdxMint.toBase58() == usdcMint.toBase58()) return@firstOrNull it.serumV3Usdc != null
            if (usdxMint.toBase58() == usdtMint.toBase58()) return@firstOrNull it.serumV3Usdt != null
            return@firstOrNull false
        }

        if (usdxMint.toBase58() == usdcMint.toBase58()) {
            return token?.serumV3Usdc?.toPublicKey()
        }

        if (usdxMint.toBase58() == usdtMint.toBase58()) {
            return token?.serumV3Usdt?.toPublicKey()
        }

        return null
    }

    suspend fun usdcPathExists(
        fromMint: PublicKey,
        toMint: PublicKey
    ): Boolean {
        val tokens = userInteractor.getUserTokens()
        val fromUsdcExists = tokens.any { it.mintAddress == fromMint.toBase58() && it.serumV3Usdc != null }

        if (fromUsdcExists) return true

        return tokens.any { it.mintAddress == toMint.toBase58() && it.serumV3Usdc != null }
    }

    suspend fun route(
        fromMint: PublicKey,
        toMint: PublicKey
    ): List<PublicKey>? {

        if (fromMint.toBase58() == usdcMint.toBase58() || fromMint.toBase58() == usdtMint.toBase58()) {
            val marketAddress = getMarketAddress(fromMint, toMint)
            return marketAddress?.let { listOf(it) }
        }
        if (toMint.toBase58() == usdcMint.toBase58() || toMint.toBase58() == usdtMint.toBase58()) {
            val marketAddress = getMarketAddress(toMint, fromMint)
            return marketAddress?.let { listOf(it) }
        }

        val usdcPair = getMarketPairs(usdcMint, fromMint, toMint)
        return usdcPair ?: getMarketPairs(usdtMint, fromMint, toMint)
    }

    private suspend fun getMarketPairs(
        usdxMint: PublicKey,
        fromMint: PublicKey,
        toMint: PublicKey
    ): List<PublicKey>? {
        val from = getMarketAddress(usdxMint, fromMint)
        val to = getMarketAddress(usdxMint, toMint)

        return if (from != null && to != null) {
            listOf(from, to)
        } else null
    }
}