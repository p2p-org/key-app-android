package org.p2p.ethereumkit

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.p2p.ethereumkit.core.EthereumKit
import org.p2p.ethereumkit.repository.EthereumKitRepository

private const val ETHEREUM_ADDRESS_LENGTH = 42

class EthereumKeyPairGeneratorTest {
    private val seedPhrase = "PUT HERE YOUR SEED PHRASE".split(" ")

    @Before
    fun setup() {
        EthereumKit.init()
    }

    @Test
    fun `generate eth key pair by given seed phrase`() {
        val repository = EthereumKitRepository()
        val result = repository.generateKeyPair(seedPhrase)
        println(result.privateKey)
        assert(result.publicKey.toString().length == ETHEREUM_ADDRESS_LENGTH)
    }

    @After
    fun clear() {
    }
}
