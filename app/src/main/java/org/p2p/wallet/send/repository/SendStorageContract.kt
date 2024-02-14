package org.p2p.wallet.send.repository

import org.p2p.core.crypto.Base58String

interface SendStorageContract {
    fun restore(): Base58String?
    fun save(tokenMint: Base58String)
    fun remove()
}
