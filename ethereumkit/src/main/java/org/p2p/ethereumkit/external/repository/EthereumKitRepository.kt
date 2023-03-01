package org.p2p.ethereumkit.external.repository

import io.horizontalsystems.hdwalletkit.Mnemonic
import org.p2p.ethereumkit.internal.core.signer.Signer
import org.p2p.ethereumkit.internal.model.EthTokenKeyProvider
import org.p2p.ethereumkit.internal.models.Chain

class EthereumKitRepository : EthereumRepository {

    override fun generateKeyPair(seedPhrase: List<String>): EthTokenKeyProvider {
        val seed = Mnemonic().toSeed(seedPhrase)
        val privateKey = Signer.privateKey(seed, Chain.Ethereum)
        val publicKey = Signer.address(seed, Chain.Ethereum)
        return EthTokenKeyProvider(publicKey,privateKey)
    }
}
