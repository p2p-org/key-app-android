package org.p2p.solanaj.serumswap.model

import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.model.types.RequestConfiguration
import org.p2p.solanaj.serumswap.Market
import java.math.BigInteger

// / Swap params
// / - Parameters:
// /   - fromMint: Token mint to swap from.
// /   - toMint: Token mint to swap to.
// /   - quoteMint: Token mint used as the quote currency for a transitive swap, i.e., the connecting currency.
// /   - amount: Amount of `fromMint` to swap in exchange for `toMint`.
// /   - minExchangeRate: The minimum rate used to calculate the number of tokens one should receive for the swap. This is a safety mechanism to prevent one from performing an unexpecteed trade.
// /   - referral: Token account to receive the Serum referral fee. The mint must be in the quote currency of the trade (USDC or USDT).
// /   - fromWallet: Wallet for `fromMint`. If not provided, uses an associated token address for the configured provider.
// /   - toWallet: Wallet for `toMint`. If not provided, an associated token account will be created for the configured provider.
// /   - quoteWallet: Wallet of the quote currency to use in a transitive swap. Should be either a USDC or USDT wallet. If not provided an associated token account will be created for the configured provider.
// /   - fromMarket: Market client for the first leg of the swap. Can be given to prevent the client from making unnecessary network requests.
// /   - toMarket: Market client for the second leg of the swap. Can be given to prevent the client from making unnecessary network requests.
// /   - fromOpenOrders: Open orders account for the first leg of the swap. If not given, an open orders account will be created.
// /   - toOpenOrders: Open orders account for the second leg of the swap. If not given, an open orders account will be created.
// /   - options: RPC options. If not given the options on the program's provider are used.
// /   - close: True if all new open orders accounts should be automatically closed. Currently disabled.
// /   - feePayer: The payer that pays the creation transaction. Nil if the current user is the payer
// /   - additionalTransactions: Additional transactions to bundle into the swap transaction

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
    val close: Boolean,
    val feePayer: PublicKey? = null,
    val additionalTransactions: List<SignersAndInstructions>? = null
)
