package org.p2p.ethereumkit.external.model

import io.horizontalsystems.hdwalletkit.Mnemonic
import org.p2p.ethereumkit.internal.core.signer.Signer
import org.p2p.ethereumkit.internal.models.EthAddress
import org.p2p.ethereumkit.internal.models.Chain
import java.math.BigInteger

class EthTokenKeyProvider(
    private val seedPhrase: List<String>
) {

    private val mnemonic: ByteArray = Mnemonic().toSeed(seedPhrase)

    val address: EthAddress = Signer.address(mnemonic, Chain.Ethereum)

    val privateKey: BigInteger = Signer.privateKey(mnemonic, Chain.Ethereum)
}
