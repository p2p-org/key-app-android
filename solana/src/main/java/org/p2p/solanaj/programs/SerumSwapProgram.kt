package org.p2p.solanaj.programs

import org.p2p.solanaj.core.AccountMeta
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Sysvar.SYSVAR_RENT_ADDRESS
import org.p2p.solanaj.core.TransactionInstruction
import org.p2p.solanaj.serumswap.Market
import org.p2p.solanaj.serumswap.model.ExchangeRate
import org.p2p.solanaj.serumswap.model.Side
import org.p2p.solanaj.serumswap.utils.SerumSwapUtils
import org.p2p.solanaj.utils.ByteUtils
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.math.BigInteger

object SerumSwapProgram {

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
        side: Side,
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
        val wallet = if (side == Side.BID) pcWallet else coinWallet
        keys.add(AccountMeta(wallet, isSigner = false, isWritable = true)) // market.order_payer_token_account
        keys.add(AccountMeta(market.coinVault, isSigner = false, isWritable = true))
        keys.add(AccountMeta(market.pcVault, isSigner = false, isWritable = true))
        keys.add(AccountMeta(vaultSigner, isSigner = false, isWritable = false))
        keys.add(AccountMeta(coinWallet, isSigner = false, isWritable = true))
        keys.add(AccountMeta(authority, isSigner = true, isWritable = false))
        keys.add(AccountMeta(pcWallet, isSigner = false, isWritable = true))
        keys.add(AccountMeta(dexPID, isSigner = false, isWritable = false))
        keys.add(AccountMeta(TokenProgram.PROGRAM_ID, isSigner = false, isWritable = false))
        keys.add(AccountMeta(TokenProgram.PROGRAM_ID, isSigner = false, isWritable = false))

        val sighashByteArray = SerumSwapUtils.sighash(ixName = "swap")

        // 4 byte instruction index + 8 bytes lamports
        val bos = ByteArrayOutputStream()
        bos.write(sighashByteArray)
        bos.write(side.getBytes())
        try {
            ByteUtils.uint64ToByteStreamLE(amount, bos)
            ByteUtils.uint64ToByteStreamLE(minExchangeRate.rate, bos)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        bos.write(minExchangeRate.fromDecimals)
        bos.write(minExchangeRate.quoteDecimals)
        bos.write(minExchangeRate.strictBytes)

        return TransactionInstruction(serumSwapPID, keys, bos.toByteArray())
    }

    fun transitiveSwapInstruction(
        authority: PublicKey,
        fromMarket: Market,
        toMarket: Market,
        fromVaultSigner: PublicKey,
        toVaultSigner: PublicKey,
        fromOpenOrder: PublicKey,
        toOpenOrder: PublicKey,
        fromWallet: PublicKey,
        toWallet: PublicKey,
        amount: BigInteger,
        minExchangeRate: ExchangeRate,
        pcWallet: PublicKey,
        referral: PublicKey?
    ): TransactionInstruction {
        val keys = ArrayList<AccountMeta>()

        keys.add(AccountMeta(fromMarket.address, isSigner = false, isWritable = true))
        keys.add(AccountMeta(fromOpenOrder, isSigner = false, isWritable = true))
        keys.add(AccountMeta(fromMarket.requestQueue, isSigner = false, isWritable = true))
        keys.add(AccountMeta(fromMarket.eventQueue, isSigner = false, isWritable = true))
        keys.add(AccountMeta(fromMarket.bidsAddress, isSigner = false, isWritable = true))
        keys.add(AccountMeta(fromMarket.asksAddress, isSigner = false, isWritable = true))
        keys.add(AccountMeta(fromWallet, isSigner = false, isWritable = true)) // from.order_payer_token_accoun)t
        keys.add(AccountMeta(fromMarket.coinVault, isSigner = false, isWritable = true))
        keys.add(AccountMeta(fromMarket.pcVault, isSigner = false, isWritable = true))
        keys.add(AccountMeta(fromVaultSigner, isSigner = false, isWritable = false))
        keys.add(AccountMeta(fromWallet, isSigner = false, isWritable = true))
        keys.add(AccountMeta(toMarket.address, isSigner = false, isWritable = true))
        keys.add(AccountMeta(toOpenOrder, isSigner = false, isWritable = true))
        keys.add(AccountMeta(toMarket.requestQueue, isSigner = false, isWritable = true))
        keys.add(AccountMeta(toMarket.eventQueue, isSigner = false, isWritable = true))
        keys.add(AccountMeta(toMarket.bidsAddress, isSigner = false, isWritable = true))
        keys.add(AccountMeta(toMarket.asksAddress, isSigner = false, isWritable = true))
        keys.add(AccountMeta(pcWallet, isSigner = false, isWritable = true))
        keys.add(AccountMeta(toMarket.coinVault, isSigner = false, isWritable = true))
        keys.add(AccountMeta(toMarket.pcVault, isSigner = false, isWritable = true))
        keys.add(AccountMeta(toVaultSigner, isSigner = false, isWritable = false))
        keys.add(AccountMeta(toWallet, isSigner = false, isWritable = true))
        keys.add(AccountMeta(authority, isSigner = true, isWritable = false))
        keys.add(AccountMeta(pcWallet, isSigner = false, isWritable = true))
        keys.add(AccountMeta(dexPID, isSigner = false, isWritable = false))
        keys.add(AccountMeta(TokenProgram.PROGRAM_ID, isSigner = false, isWritable = false))
        keys.add(AccountMeta(TokenProgram.PROGRAM_ID, isSigner = false, isWritable = false))

        val sighashByteArray = SerumSwapUtils.sighash(ixName = "swapTransitive")

        // 4 byte instruction index + 8 bytes lamports
        val bos = ByteArrayOutputStream()
        bos.write(sighashByteArray)
        try {
            ByteUtils.uint64ToByteStreamLE(amount, bos)
            ByteUtils.uint64ToByteStreamLE(minExchangeRate.rate, bos)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        bos.write(minExchangeRate.fromDecimals)
        bos.write(minExchangeRate.quoteDecimals)
        bos.write(minExchangeRate.strictBytes)

        return TransactionInstruction(serumSwapPID, keys, bos.toByteArray())
    }
}
