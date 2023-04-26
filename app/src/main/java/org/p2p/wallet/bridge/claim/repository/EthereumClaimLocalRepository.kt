package org.p2p.wallet.bridge.claim.repository

import timber.log.Timber
import kotlinx.coroutines.CancellationException
import org.p2p.ethereumkit.external.model.ERC20Tokens
import org.p2p.wallet.bridge.claim.model.ClaimStatus
import org.p2p.wallet.bridge.model.BridgeBundle

class EthereumClaimLocalRepository {

    fun parseBundles(bundles: List<BridgeBundle>): Map<String, List<ClaimStatus>> {
        return try {
            val ethAddress = ERC20Tokens.ETH.contractAddress
            return bundles.associate { bundle ->
                val tokenHex = bundle.token?.hex ?: ethAddress

                val tokenBundles = bundles.filter {
                    val bundleToken = it.token?.hex ?: ethAddress
                    tokenHex == bundleToken
                }.mapNotNull { it.status }

                tokenHex to tokenBundles
            }
        } catch (cancelled: CancellationException) {
            Timber.i(cancelled)
            emptyMap()
        } catch (throwable: Throwable) {
            Timber.e(throwable, "Error on loading loadEthBundles")
            emptyMap()
        }
    }
}
