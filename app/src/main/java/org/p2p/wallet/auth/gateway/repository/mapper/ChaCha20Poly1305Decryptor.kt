package org.p2p.wallet.auth.gateway.repository.mapper

import org.bouncycastle.crypto.modes.ChaCha20Poly1305
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.crypto.params.ParametersWithIV
import org.p2p.wallet.utils.processBytesKt

class ChaCha20Poly1305Decryptor(
    private val chaCha20Poly1305: ChaCha20Poly1305
) {
    fun decryptData(
        privateKey: ByteArray,
        nonce: ByteArray,
        dataToDecrypt: ByteArray
    ): ByteArray {
        chaCha20Poly1305.init(
            false,
            ParametersWithIV(
                KeyParameter(privateKey.copyOf()),
                nonce.copyOf()
            )
        )

        val decryptionResultSize = chaCha20Poly1305.getOutputSize(dataToDecrypt.size)
        val decryptionResult = ByteArray(decryptionResultSize)

        val finalOff = chaCha20Poly1305.processBytesKt(
            inBytes = dataToDecrypt,
            len = dataToDecrypt.size,
            outBytes = decryptionResult,
        )
        chaCha20Poly1305.doFinal(decryptionResult, finalOff)
        chaCha20Poly1305.reset()

        return decryptionResult
    }
}
