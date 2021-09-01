package org.p2p.solanaj.serumswap

import org.bitcoinj.utils.ExchangeRate
import org.p2p.solanaj.model.core.AccountMeta
import org.p2p.solanaj.model.core.PublicKey
import org.p2p.solanaj.model.core.Sysvar.SYSVAR_RENT_ADDRESS
import org.p2p.solanaj.model.core.TransactionInstruction
import org.p2p.solanaj.model.serumswap.Market
import org.p2p.solanaj.model.serumswap.SerumSwap
import java.math.BigInteger
import java.util.ArrayList

object SerumSwapInstructions {

    val usdcMint = PublicKey("EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v")
    val usdtMint = PublicKey("Es9vMFrzaCERmJfrF4H2FYD4KCoNkY11McCe8BenwNYB")
    val dexPID = PublicKey("9xQeWvG816bUx9EPjHmaT23yvVM2ZWbrrpZb9PusVFin")
    val serumSwapPID = PublicKey("22Y43yTVxuUkoRKdm9thyRhQ3SdgQS7c7kB6UNCiaczD")

    fun closeOrderInstruction(
        order: PublicKey,
        marketAddress: PublicKey,
        owner: PublicKey,
        destination: PublicKey,
    ): TransactionInstruction {
        val keys = ArrayList<AccountMeta>()

        keys.add(AccountMeta(order, isSigner = false, isWritable = true))
        keys.add(AccountMeta(owner, isSigner = true, isWritable = false))
        keys.add(AccountMeta(destination, isSigner = false, isWritable = true))
        keys.add(AccountMeta(marketAddress, isSigner = false, isWritable = false))
        keys.add(AccountMeta(dexPID, isSigner = false, isWritable = false))

        return TransactionInstruction(serumSwapPID, keys, byteArrayOf())
    }

    fun initOrderInstruction(
        order: PublicKey,
        marketAddress: PublicKey,
        owner: PublicKey
    ): TransactionInstruction {
        val keys = ArrayList<AccountMeta>()

        keys.add(AccountMeta(order, isSigner = false, isWritable = true))
        keys.add(AccountMeta(owner, isSigner = true, isWritable = false))
        keys.add(AccountMeta(marketAddress, isSigner = false, isWritable = false))
        keys.add(AccountMeta(dexPID, isSigner = false, isWritable = false))
        keys.add(AccountMeta(SYSVAR_RENT_ADDRESS, isSigner = false, isWritable = false))

        return TransactionInstruction(serumSwapPID, keys, byteArrayOf())
    }

    fun directSwapInstruction(
        authority: PublicKey,
        side: SerumSwap.Side,
        amount: BigInteger,
        minExchangeRate: ExchangeRate,
        market: Market,
        vaultSigner: PublicKey,
        openOrders: PublicKey,
        pcWallet: PublicKey,
        coinWallet: PublicKey,
        referral: PublicKey?
    ): TransactionInstruction {
        val keys = ArrayList<AccountMeta>()

        keys.add(AccountMeta(market.address, isSigner = false, isWritable = true))
        keys.add(AccountMeta(openOrders, isSigner = false, isWritable = true))
        keys.add(AccountMeta(market.requestQueue, isSigner = false, isWritable = true))
        keys.add(AccountMeta(market.eventQueue, isSigner = false, isWritable = true))
        keys.add(AccountMeta(market.bidsAddress, isSigner = false, isWritable = true))
        keys.add(AccountMeta(market.asksAddress, isSigner = false, isWritable = true))
        val wallet = if (side == SerumSwap.Side.BID) pcWallet else coinWallet
        keys.add(AccountMeta(wallet, isSigner = false, isWritable = true)) // market.order_payer_token_account
        keys.add(AccountMeta(market.bidsAddress, isSigner = false, isWritable = true))
        keys.add(AccountMeta(market.bidsAddress, isSigner = false, isWritable = true))


        return TransactionInstruction(serumSwapPID, keys, byteArrayOf())
    }
}