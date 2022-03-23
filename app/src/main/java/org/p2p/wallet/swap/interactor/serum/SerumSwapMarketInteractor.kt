package org.p2p.wallet.swap.interactor.serum

import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.toPublicKey
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.programs.SerumSwapProgram.usdcMint
import org.p2p.solanaj.programs.SerumSwapProgram.usdtMint

class SerumSwapMarketInteractor(
    private val userInteractor: UserInteractor
) {

    // / Returns the `usdxMint` quoted market address.
    suspend fun getMarketAddress(
        usdxMint: PublicKey,
        baseMint: PublicKey
    ): PublicKey? {

        val usdxMintDecoded = usdxMint.toBase58()

        val tokens = userInteractor.getUserTokens()

        val token = tokens.firstOrNull {
            if (it.mintAddress != baseMint.toBase58()) return@firstOrNull false
            if (usdxMintDecoded == usdcMint.toBase58()) return@firstOrNull it.serumV3Usdc != null
            if (usdxMintDecoded == usdtMint.toBase58()) return@firstOrNull it.serumV3Usdt != null
            return@firstOrNull false
        }

        if (usdxMintDecoded == usdcMint.toBase58()) {
            return token?.serumV3Usdc?.toPublicKey()
        }

        if (usdxMintDecoded == usdtMint.toBase58()) {
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

    // Returns a list of markets to trade across to swap `fromMint` to `toMint`
    suspend fun route(
        fromMint: PublicKey,
        toMint: PublicKey
    ): List<PublicKey>? {

        val fromMintDecoded = fromMint.toBase58()
        val toMintDecoded = toMint.toBase58()

        val usdcDecoded = usdcMint.toBase58()
        val usdtDecoded = usdtMint.toBase58()

        if (fromMintDecoded == usdcDecoded || fromMintDecoded == usdtDecoded) {
            val marketAddress = getMarketAddress(fromMint, toMint)
            return marketAddress?.let { listOf(it) }
        }
        if (toMintDecoded == usdcDecoded || toMintDecoded == usdtDecoded) {
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

        return if (from != null && to != null) listOf(from, to) else null
    }
}
