package org.p2p.wallet.auth.gateway.repository.mapper

import org.bitcoinj.crypto.MnemonicCode
import org.p2p.solanaj.crypto.Bip44CustomDerivation
import org.p2p.solanaj.utils.TweetNaclFast
import org.p2p.wallet.utils.emptyString

class ChaCha20Poly1305SymmetricKeyGenerator {

    companion object {
        const val SYMMETRIC_KEY_SIZE = 32
    }

    private val derivationPath = Bip44CustomDerivation(
        purpose = 44,
        type = 101,
        account = 0,
        change = 0,
        isHardened = true
    )

    /**
     * derivation path used: m/44h/101h/0h/0h
     */
    fun generateSymmetricKey(seedPhrase: List<String>): ByteArray {
        val masterPrivateKey = MnemonicCode.toSeed(seedPhrase, emptyString())
        val derivedPrivateKey: ByteArray = derivationPath.derivePrivateKeyFromSeed(masterPrivateKey)

        return TweetNaclFast.Signature.keyPair_fromSeed(derivedPrivateKey)
            .secretKey
            .take(SYMMETRIC_KEY_SIZE)
            .toByteArray()
    }
}
