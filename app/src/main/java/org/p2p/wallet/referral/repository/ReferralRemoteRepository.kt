package org.p2p.wallet.referral.repository

import timber.log.Timber
import kotlinx.coroutines.withContext
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.solanaj.model.types.RpcMapRequest
import org.p2p.solanaj.utils.SolanaMessageSigner
import org.p2p.wallet.common.feature_toggles.toggles.remote.ReferralProgramEnabledFeatureToggle
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider

class ReferralRemoteRepository(
    private val api: ReferralApi,
    private val tokenKeyProvider: TokenKeyProvider,
    private val messageSigner: SolanaMessageSigner,
    private val dispatchers: CoroutineDispatchers,
    private val referralEnabledFt: ReferralProgramEnabledFeatureToggle
) : ReferralRepository {

    override suspend fun setReferent(referentUsernameOrPublicKey: String) {
        if (referralEnabledFt.isFeatureEnabled) {
            setReferentInternal(referentUsernameOrPublicKey)
        }
    }

    private suspend fun setReferentInternal(referent: String): Unit = withContext(dispatchers.io) {
        try {
            val requestTimestamp = System.currentTimeMillis()
            val timedSignature = mapOf(
                "signature" to messageSigner.signMessage(
                    message = requestTimestamp.toString().toByteArray(),
                    keyPair = tokenKeyProvider.keyPair
                ),
                "timestamp" to requestTimestamp
            )

            val params = buildMap {
                this += "user" to tokenKeyProvider.publicKey
                this += "referent" to referent
                this += "timed_signature" to timedSignature
            }

            val rpcRequest = RpcMapRequest("set_referent", params)
            api.setReferent(rpcRequest)
        } catch (error: Throwable) {
            Timber.e(error, "Failed to set referent")
        }
    }
}
