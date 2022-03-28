package org.p2p.solanaj.kits.renBridge.renVM

import org.bitcoinj.core.Base58
import org.p2p.solanaj.kits.renBridge.renVM.types.ParamsSubmitMint
import org.p2p.solanaj.utils.ByteUtils
import org.p2p.solanaj.utils.Hash
import org.p2p.solanaj.utils.Utils
import java.io.ByteArrayOutputStream
import java.io.IOException

object HashUtils {

    fun hashTransactionMint(
        mintTx: ParamsSubmitMint.MintTransactionInput,
        selector: String
    ): ByteArray {

        val out = ByteArrayOutputStream()
        val version = "1"

        try {
            out.write(marshalString(version))
            out.write(marshalString(selector))

            val marshalledMintTransactionInput =
                MINT_TRANSACTION_INPUT
            out.write(Base58.decode(marshalledMintTransactionInput))

            out.write(marshalBytes(Utils.fromURLBase64(mintTx.txid)))
            out.write(ByteUtils.uint32ToByteArrayBE(mintTx.txid.toLong()))
            out.write(Utils.amountToUint256ByteArrayBE(mintTx.amount))
            out.write(byteArrayOf(0, 0, 0, 0))
            out.write(Utils.fromURLBase64(mintTx.phash))
            out.write(Utils.fromURLBase64(mintTx.nhash))
            out.write(marshalBytes(Utils.fromURLBase64(mintTx.gpubkey)))
            out.write(Utils.fromURLBase64(mintTx.ghash))
        } catch (e: IOException) {
            // TODO provide log error here
        }
        return Hash.sha256(out.toByteArray())
    }

    fun marshalString(src: String): ByteArray {
        return marshalBytes(src.toByteArray())
    }

    fun marshalBytes(byteArray: ByteArray): ByteArray {
        val out = ByteArray(ByteUtils.UINT_32_LENGTH + byteArray.size)
        val uint32Array = ByteUtils.uint32ToByteArrayBE(byteArray.size.toLong())
        System.arraycopy(uint32Array, 0, out, 0, ByteUtils.UINT_32_LENGTH)
        System.arraycopy(byteArray, 0, out, ByteUtils.UINT_32_LENGTH, byteArray.size)
        return out
    }
}
