package org.p2p.solanaj.kits.renBridge.renVM

import android.util.Log
import org.bitcoinj.core.Base58
import org.p2p.solanaj.kits.renBridge.renVM.types.ParamsSubmitMint
import org.p2p.solanaj.utils.ByteUtils
import org.p2p.solanaj.utils.Hash
import org.p2p.solanaj.utils.Utils
import java.io.ByteArrayOutputStream
import java.io.IOException

fun ParamsSubmitMint.MintTransactionInput.hashTransactionMint(selector: String): ByteArray {

    val out = ByteArrayOutputStream()
    val version = "1"

    try {
        out.write(marshalString(version))
        out.write(marshalString(selector))

        val marshalledMintTransactionInput = MINT_TRANSACTION_INPUT
        out.write(Base58.decode(marshalledMintTransactionInput))

        out.write(marshalBytes(Utils.fromURLBase64(txid)))
        out.write(ByteUtils.uint32ToByteArrayBE(txindex.toLong()))
        out.write(Utils.amountToUint256ByteArrayBE(amount))
        out.write(byteArrayOf(0, 0, 0, 0))
        out.write(Utils.fromURLBase64(phash))
        out.write(marshalString(to))
        out.write(Utils.fromURLBase64(nonce))
        out.write(Utils.fromURLBase64(nhash))
        out.write(marshalBytes(Utils.fromURLBase64(gpubkey)))
        out.write(Utils.fromURLBase64(ghash))
    } catch (e: IOException) {
        Log.e("HashUtils", "Error on hashing transaction mint $e")
    }
    out.close()
    return Hash.sha256(out.toByteArray())
}

private fun marshalString(src: String): ByteArray {
    return marshalBytes(src.toByteArray())
}

private fun marshalBytes(byteArray: ByteArray): ByteArray {
    val out = ByteArray(ByteUtils.UINT_32_LENGTH + byteArray.size)
    val uint32Array = ByteUtils.uint32ToByteArrayBE(byteArray.size.toLong())
    System.arraycopy(uint32Array, 0, out, 0, ByteUtils.UINT_32_LENGTH)
    System.arraycopy(byteArray, 0, out, ByteUtils.UINT_32_LENGTH, byteArray.size)
    return out
}
