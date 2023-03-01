package org.p2p.ethereumkit.repository

import org.p2p.ethereumkit.model.EthTokenKeyProvider

interface EthereumRepository {

    fun generateKeyPair(seedPhrase: List<String>): EthTokenKeyProvider
}
