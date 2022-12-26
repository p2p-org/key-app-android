package org.p2p.wallet.moonpay.repository.sell

import org.bitcoinj.crypto.MnemonicCode
import org.p2p.core.utils.emptyString
import org.p2p.solanaj.crypto.Bip44CustomDerivation
import org.p2p.solanaj.utils.TweetNaclFast
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseProvider
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.toBase58Instance

class MoonpayExternalCustomerIdProvider(private val seedPhraseProvider: SeedPhraseProvider) {
    private var cachedCustomerId: Base58String? = null

    private val derivationPath = Bip44CustomDerivation.BIP_44_501_101_0

    fun getCustomerId(): Base58String = cachedCustomerId ?: run { generateCustomerId() }

    private fun generateCustomerId(): Base58String {
        val masterPrivateKey = MnemonicCode.toSeed(seedPhraseProvider.getUserSeedPhrase().seedPhrase, emptyString())
        val derivedPrivateKey: ByteArray = derivationPath.derivePrivateKeyFromSeed(masterPrivateKey)
        return TweetNaclFast.Signature.keyPair_fromSeed(derivedPrivateKey)
            .publicKey
            .toBase58Instance()
    }
}
