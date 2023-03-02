package org.p2p.ethereumkit

import org.junit.Test
import org.p2p.ethereumkit.external.repository.EthereumKitRepository

private const val ETHEREUM_ADDRESS_LENGTH = 42

class EthereumKeyPairGeneratorTest {
    //This seed phrase was taken from ethereum kit repository, so feel free to use it
    private val seedPhrase = "apart approve black comfort steel spin real renew tone primary key cherry".split(" ")

    @Test
    fun `generate eth key pair by given seed phrase`() {
        val repository = EthereumKitRepository()
        val result = repository.generateKeyPair(seedPhrase)
        println("Private key: ${result.privateKey}")
        println("Public key: ${result.publicKey}")
        assert(result.publicKey.toString().length == ETHEREUM_ADDRESS_LENGTH)
    }
}
