package org.p2p.wallet.swap.interactor.serum

import android.util.Base64
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.solanaj.serumswap.Market
import org.p2p.solanaj.serumswap.MarketStatLayout
import org.p2p.solanaj.serumswap.MarketStatLayoutParser
import org.p2p.solanaj.serumswap.orderbook.Orderbook
import org.p2p.wallet.rpc.repository.account.RpcAccountRepository

class SerumMarketInteractor(
    private val rpcRepository: RpcAccountRepository
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

        val decoded = getAccountInfoAndVerifyOwner(address, layoutType)

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

    private suspend fun getMintData(account: PublicKey): TokenProgram.MintData {
        val info = rpcRepository.getAccountInfo(account.toBase58())
            ?: throw IllegalStateException("No mint data")

        if (info.value?.owner != TokenProgram.PROGRAM_ID.toBase58()) {
            throw IllegalStateException("Invalid mint owner")
        }

        val base64Data = info.value.data!![0]
        val data = Base64.decode(base64Data, Base64.DEFAULT)

        return TokenProgram.MintData.decode(data)
    }

    private suspend fun getAccountInfoAndVerifyOwner(
        account: PublicKey,
        type: MarketStatLayout.Type
    ): MarketStatLayout {

        val info = rpcRepository.getAccountInfo(account.toBase58())
            ?: throw IllegalStateException("No address data")

        val base64Data = info.value.data!![0]
        val data = Base64.decode(base64Data, Base64.DEFAULT)
        return if (type == MarketStatLayout.Type.LAYOUT_V1) {
            MarketStatLayoutParser.parseV1(data)
        } else {
            MarketStatLayoutParser.parseV2(data)
        }
    }

    private suspend fun loadOrderbook(market: Market, address: PublicKey): Orderbook {
        val account = rpcRepository.getAccountInfo(address.toBase58())
        if (account?.value == null) throw IllegalStateException("Invalid account info")

        val base64Data = account.value.data!![0]
        val data = Base64.decode(base64Data, Base64.DEFAULT)
        val layout = Orderbook.Layout(data)
        return Orderbook(market, layout.accountFlags, layout.slab)
    }
}
