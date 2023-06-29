package org.p2p.ethereumkit.internal.core

import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

import org.p2p.ethereumkit.internal.crypto.InternalBouncyCastleProvider

class EthereumKit() {

    companion object {

        fun init() {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
            Security.addProvider(InternalBouncyCastleProvider.getInstance())
        }
    }
}
