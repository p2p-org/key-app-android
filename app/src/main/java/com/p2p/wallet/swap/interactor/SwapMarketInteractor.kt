package com.p2p.wallet.swap.interactor

import android.util.Base64
import com.p2p.wallet.rpc.repository.RpcRepository
import com.p2p.wallet.user.interactor.UserInteractor
import com.p2p.wallet.utils.toPublicKey
import org.p2p.solanaj.model.core.PublicKey
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.solanaj.serumswap.Market
import org.p2p.solanaj.serumswap.MarketStatLayout
import org.p2p.solanaj.serumswap.instructions.SerumSwapInstructions.usdcMint
import org.p2p.solanaj.serumswap.instructions.SerumSwapInstructions.usdtMint
import org.p2p.solanaj.serumswap.orderbook.Orderbook

class SwapMarketInteractor(
    private val rpcRepository: RpcRepository,
    private val userInteractor: UserInteractor
) {

    // Load a market base on its address
    suspend fun loadMarket(
        address: PublicKey,
        skipPreflight: Boolean = false,
        commitment: String = "recent",
        programId: PublicKey,
        layoutOverride: MarketStatLayout.Type? = null
    ): Market {

        val layoutType = layoutOverride ?: Market.getLayoutType(programId.toBase58())

        val decoded = getAccountInfoAndVerifyOwner(address, programId, layoutType)

        if (!decoded.accountFlags.initialized ||
            !decoded.accountFlags.market ||
            decoded.ownAddress.toBase58() != address.toBase58()
        ) {
            throw IllegalStateException("Market invalid")
        }

        val baseMintDecimals = getMintData(decoded.baseMint).decimals
        val quoteMintDecimals = getMintData(decoded.quoteMint).decimals

        return Market(
            programId = programId,
            decoded = decoded,
            baseSplTokenDecimals = baseMintDecimals,
            quoteSplTokenDecimals = quoteMintDecimals,
            skipPreflight = skipPreflight,
            commitment = commitment,
            layoutOverride = layoutOverride
        )
    }

    suspend fun loadBids(market: Market): Orderbook =
        loadOrderbook(market, market.bidsAddress)

    suspend fun loadAsks(market: Market): Orderbook =
        loadOrderbook(market, market.asksAddress)

    suspend fun getMarketAddress(
        usdxMint: PublicKey,
        baseMint: PublicKey
    ): PublicKey? {

        val tokens = userInteractor.getTokens()

        val token = tokens.firstOrNull {
            if (it.mintAddress != baseMint.toBase58()) return@firstOrNull false
            if (usdxMint.toBase58() == usdcMint.toBase58()) return@firstOrNull it.serumV3Usdc != null
            if (usdxMint.toBase58() == usdcMint.toBase58()) return@firstOrNull it.serumV3Usdc != null
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
        val tokens = userInteractor.getTokens()
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

    private suspend fun getMintData(account: PublicKey): TokenProgram.MintData {
        val info = rpcRepository.getAccountInfo(account)
            ?: throw IllegalStateException("No mint data")

        if (info.value != null && info.value?.owner == TokenProgram.PROGRAM_ID.toBase58()) {
            throw IllegalStateException("Address not owned by program")
        }

        val base64Data = info.value.data!![0]
        val data = Base64.decode(base64Data, Base64.DEFAULT)

        return TokenProgram.MintData.decode(data)
    }

    private suspend fun getAccountInfoAndVerifyOwner(
        account: PublicKey,
        programId: PublicKey,
        type: MarketStatLayout.Type
    ): MarketStatLayout {

        val info = rpcRepository.getAccountInfo(account)
            ?: throw IllegalStateException("No address data")

        if (info.value != null && info.value?.owner == programId.toBase58()) {
            throw IllegalStateException("Address not owned by program")
        }

        val base64Data = info.value.data!![0]
        val data = Base64.decode(base64Data, Base64.DEFAULT)
        return if (type == MarketStatLayout.Type.LAYOUT_V1) {
            MarketStatLayout.LayoutV1(data)
        } else {
            MarketStatLayout.LayoutV2(data)
        }
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

    private suspend fun loadOrderbook(market: Market, address: PublicKey): Orderbook {
        val account = rpcRepository.getAccountInfo(address)
        if (account?.value == null) throw IllegalStateException("Invalid account info")

        val base64Data = account.value.data!![0]
        val data = Base64.decode(base64Data, Base64.DEFAULT)
        val layout = Orderbook.Layout(data)
        return Orderbook(market, layout.accountFlags, layout.slab)
    }
}