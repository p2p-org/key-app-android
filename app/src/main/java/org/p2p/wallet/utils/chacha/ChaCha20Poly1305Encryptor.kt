package org.p2p.wallet.utils.chacha

import org.bouncycastle.crypto.modes.ChaCha20Poly1305
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.crypto.params.ParametersWithIV
import org.p2p.wallet.utils.processBytesKt

class ChaCha20Poly1305Encryptor(
    private val chaCha20Poly1305: ChaCha20Poly1305
) {
    fun encryptData(
        privateKey: ByteArray,
        nonce: ByteArray,
        dataToEncrypt: ByteArray
    ): ByteArray {
        chaCha20Poly1305.init(
            true,
            ParametersWithIV(
                KeyParameter(privateKey.copyOf()),
                nonce.copyOf()
            )
        )

        val resultSize = chaCha20Poly1305.getOutputSize(dataToEncrypt.size)
        val encryptionResult = ByteArray(resultSize)

        val finalOff = chaCha20Poly1305.processBytesKt(
            inBytes = dataToEncrypt,
            len = dataToEncrypt.size,
            outBytes = encryptionResult,
        )
        chaCha20Poly1305.doFinal(encryptionResult, finalOff)

        chaCha20Poly1305.reset()
        return encryptionResult
    }
}
