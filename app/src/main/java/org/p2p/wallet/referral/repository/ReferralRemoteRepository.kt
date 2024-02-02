package org.p2p.wallet.referral.repository

import com.google.gson.Gson
import org.near.borshj.BorshBuffer
import timber.log.Timber
import java.net.URI
import java.util.Optional
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.withContext
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.rpc.JsonRpc
import org.p2p.core.rpc.RpcApi
import org.p2p.core.rpc.RpcResponse
import org.p2p.core.wrapper.eth.stripHexPrefix
import org.p2p.core.wrapper.eth.toHexString
import org.p2p.solanaj.utils.SolanaMessageSigner
import org.p2p.wallet.auth.gateway.repository.mapper.write
import org.p2p.wallet.common.feature_toggles.toggles.remote.ReferralProgramEnabledFeatureToggle
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider

class ReferralRemoteRepository(
    private val api: RpcApi,
    private val gson: Gson,
    private val tokenKeyProvider: TokenKeyProvider,
    private val messageSigner: SolanaMessageSigner,
    private val dispatchers: CoroutineDispatchers,
    private val referralEnabledFt: ReferralProgramEnabledFeatureToggle
) : ReferralRepository {

    private val url = URI("https://referral.key.app/")

    override suspend fun registerReferent() {
        if (referralEnabledFt.isFeatureEnabled) {
            registerReferentInternal()
        }
    }

    private suspend fun registerReferentInternal() {
        try {
            val requestTimestamp = System.currentTimeMillis().milliseconds.inWholeSeconds
            val signature = signRequestHex(
                tokenKeyProvider.publicKey, Optional.empty<String>(), requestTimestamp
            )
            val request = ReferralRequest(
                request = ReferralBodyRequest(
                    userPublicKey = tokenKeyProvider.publicKey,
                    referent = null,
                    timedSignature = mapOf(
                        "timestamp" to requestTimestamp,
                        "signature" to signature
                    )
                ),
                method = "register"
            )
            val response: RpcResponse = api.launch(url, gson.toJson(request))
            request.parseResponse(response, gson)
        } catch (rpcError: JsonRpc.ResponseError) {
            val message = rpcError.message ?: return
            if (message.contains("duplicate key value violates unique constraint")) {
                Timber.i("User already registered")
            } else {
                Timber.e(rpcError, "Failed to register referent")
            }
        } catch (error: Throwable) {
            Timber.e(error, "Failed to register referent")
        }
    }

    override suspend fun setReferent(referentUsernameOrPublicKey: String) {
        if (referralEnabledFt.isFeatureEnabled) {
            setReferentInternal(referentUsernameOrPublicKey)
        }
    }

    private suspend fun setReferentInternal(referent: String): Unit = withContext(dispatchers.io) {
        try {
            val requestTimestamp = System.currentTimeMillis().milliseconds.inWholeSeconds
            val signature = signRequestHex(
                tokenKeyProvider.publicKey, referent, requestTimestamp
            )
            val request = ReferralRequest(
                request = ReferralBodyRequest(
                    userPublicKey = tokenKeyProvider.publicKey,
                    referent = referent,
                    timedSignature = mapOf(
                        "timestamp" to requestTimestamp,
                        "signature" to signature
                    )
                ),
                method = "set_referent"
            )
            val response: RpcResponse = api.launch(url, gson.toJson(request))
            request.parseResponse(response, gson)
        } catch (rpcError: JsonRpc.ResponseError) {
            val message = rpcError.message ?: return@withContext
            if (message.contains("duplicate key value violates unique constraint")) {
                Timber.i("User already registered")
            } else {
                Timber.e(rpcError, "Failed to register referent")
            }
        } catch (error: Throwable) {
            Timber.e(error, "Failed to set referent")
        }
    }

    private fun signRequestHex(
        vararg args: Any,
    ): String {
        val tuple = BorshBuffer.allocate(2052)
            // order is important
            .write(*args)
            .toByteArray()

        return messageSigner.signMessage(
            message = tuple,
            keyPair = tokenKeyProvider.keyPair
        )
            .decodeToBytes()
            .toHexString()
            .stripHexPrefix()
    }
}
