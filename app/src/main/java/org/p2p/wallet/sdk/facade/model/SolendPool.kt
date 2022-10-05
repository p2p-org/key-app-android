package org.p2p.wallet.sdk.facade.model

import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.toBase58Instance

enum class SolendPool(val poolName: String, val poolAddress: Base58String) {
    MAIN("main", "4UpD2fh7xH3VP9QQaXtsS1YY3bxzWhtfpks7FatyKvdY".toBase58Instance())
}
