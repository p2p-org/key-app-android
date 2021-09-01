package org.p2p.solanaj.model.serumswap.model

import org.bitcoinj.utils.ExchangeRate
import org.p2p.solanaj.model.core.PublicKey
import org.p2p.solanaj.model.serumswap.Market
import java.math.BigInteger

data class SwapParams(
    val fromMint: PublicKey,
    val toMint: PublicKey,
    val amount: BigInteger,
    val minExchangeRate: ExchangeRate,
    val referral: PublicKey?,
    val fromWallet: PublicKey?,
    val toWallet: PublicKey?,
    val quoteWallet: PublicKey?,
    val fromMarket: Market,
    val toMarket: Market?,
    val fromOpenOrders: PublicKey?,
    val toOpenOrders: PublicKey?,
    val options: RequestConfiguration? = null,
    val close:,
    val feePayer: PublicKey? = null,
    val additionalTransactions: List<SignersAndInstructions>? = null
)