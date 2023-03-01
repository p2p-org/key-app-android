package org.p2p.ethereumkit.external.repository

import org.p2p.ethereumkit.internal.model.EthTokenKeyProvider

interface EthereumRepository {

    fun generateKeyPair(seedPhrase: List<String>): EthTokenKeyProvider
}
