package org.p2p.wallet.moonpay.repository.sell

import org.bitcoinj.crypto.MnemonicCode
import org.p2p.core.utils.emptyString
import org.p2p.solanaj.crypto.Bip44CustomDerivation
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseProvider
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.toBase58Instance
import org.p2p.solanaj.utils.TweetNaclFast

class MoonpayExternalCustomerIdProvider(private val seedPhraseProvider: SeedPhraseProvider) {
    private class MoonpayCustomerIdGenerationFailed(override val cause: Throwable) :
        Throwable("Failed to generate customer ID for Moonpay")

    private var cachedCustomerId: Base58String? = null

    private val derivationPath = Bip44CustomDerivation.BIP_44_501_101_0

    fun getCustomerId(): Base58String = cachedCustomerId ?: run { generateCustomerId() }

    private fun generateCustomerId(): Base58String = try {
        val userSeedPhrase = seedPhraseProvider.getUserSeedPhrase().seedPhrase
        check(userSeedPhrase.isNotEmpty()) { "User seed phrase is empty" }

        val masterPrivateKey = MnemonicCode.toSeed(userSeedPhrase, emptyString())
        val derivedPrivateKey: ByteArray = derivationPath.derivePrivateKeyFromSeed(masterPrivateKey)

        TweetNaclFast.Signature.keyPair_fromSeed(derivedPrivateKey)
            .publicKey
            .toBase58Instance()
    } catch (generationError: Throwable) {
        throw MoonpayCustomerIdGenerationFailed(generationError)
    }
}
