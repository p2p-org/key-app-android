package org.p2p.wallet.infrastructure.network.environment

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class NetworkEnvironment(val endpoint: String) : Parcelable {
    MAINNET("https://api.mainnet-beta.solana.com"),
    SOLANA("https://solana-api.projectserum.com"),
    RPC_POOL("https://p2p.rpcpool.com"),
    PUSH_SERVICE(" https://push-service.keyapp.org"),
    DEVNET("https://api.devnet.solana.com");
}
