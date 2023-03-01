package org.p2p.ethereumkit.repository

import io.horizontalsystems.hdwalletkit.Mnemonic
import org.p2p.ethereumkit.core.signer.Signer
import org.p2p.ethereumkit.model.EthTokenKeyProvider
import org.p2p.ethereumkit.models.Chain

class EthereumKitRepository : EthereumRepository {

    override fun generateKeyPair(seedPhrase: List<String>): EthTokenKeyProvider {
        val seed = Mnemonic().toSeed(seedPhrase)
        val privateKey = Signer.privateKey(seed, Chain.Ethereum)
        val publicKey = Signer.address(seed, Chain.Ethereum)
        return EthTokenKeyProvider(publicKey,privateKey)
    }
}
