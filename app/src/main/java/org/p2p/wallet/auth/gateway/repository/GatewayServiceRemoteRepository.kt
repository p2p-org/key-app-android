package org.p2p.wallet.auth.gateway.repository

import com.google.gson.Gson
import com.google.gson.JsonObject
import timber.log.Timber
import java.net.URI
import kotlin.time.DurationUnit
import kotlinx.coroutines.withContext
import org.p2p.core.rpc.JsonRpc
import org.p2p.core.rpc.RpcApi
import org.p2p.wallet.auth.gateway.api.GatewayServiceApi
import org.p2p.wallet.auth.gateway.api.request.OtpMethod
import org.p2p.wallet.auth.gateway.api.response.ConfirmRestoreWalletResponse
import org.p2p.wallet.auth.gateway.api.response.GatewayServiceStandardResponse
import org.p2p.wallet.auth.gateway.api.response.RegisterWalletResponse
import org.p2p.wallet.auth.gateway.api.response.UpdateMetadataResponse
import org.p2p.wallet.auth.gateway.repository.mapper.GatewayResult
import org.p2p.wallet.auth.gateway.repository.mapper.GatewayServiceCreateWalletMapper
import org.p2p.wallet.auth.gateway.repository.mapper.GatewayServiceErrorMapper
import org.p2p.wallet.auth.gateway.repository.mapper.GatewayServiceGetOnboardingMetadataMapper
import org.p2p.wallet.auth.gateway.repository.mapper.GatewayServiceRestoreWalletMapper
import org.p2p.wallet.auth.gateway.repository.mapper.GatewayServiceUpdateMetadataMapper
import org.p2p.wallet.auth.gateway.repository.model.GatewayOnboardingMetadata
import org.p2p.wallet.auth.gateway.repository.model.RestoreWalletPublicKeyError
import org.p2p.wallet.auth.model.PhoneNumber
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthSignUpResponse
import org.p2p.core.common.di.AppScope
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.network.environment.NetworkServicesUrlProvider
import org.p2p.core.crypto.Base58String
import org.p2p.wallet.utils.FlowDurationTimer

private const val TAG = "GatewayServiceRemoteRepository"

class GatewayServiceRemoteRepository(
    private val api: GatewayServiceApi,
    private val rpcApi: RpcApi,
    private val gson: Gson,
    urlProvider: NetworkServicesUrlProvider,
    private val createWalletMapper: GatewayServiceCreateWalletMapper,
    private val restoreWalletMapper: GatewayServiceRestoreWalletMapper,
    private val getOnboardingMetadataMapper: GatewayServiceGetOnboardingMetadataMapper,
    private val updateMetadataMapper: GatewayServiceUpdateMetadataMapper,
    private val errorMapper: GatewayServiceErrorMapper,
    private val dispatchers: CoroutineDispatchers,
    private val appScope: AppScope,
) : GatewayServiceRepository {

    private val gatewayUrl = URI(urlProvider.loadGatewayServiceEnvironment().baseUrl)

    private val resetTemporaryPublicKeyTimer =
        FlowDurationTimer(startValue = 10, DurationUnit.MINUTES)
            .onTimeFinished {
                Timber.i("restoreWalletPublicKey is reset")
                restoreWalletPublicKey = null
            }

    private var restoreWalletPublicKey: Base58String? = null
        set(value) {
            field = value
            if (value != null) {
                resetTemporaryPublicKeyTimer.launchTimer(appScope)
            }
        }

    override suspend fun registerWalletWithSms(
        userPublicKey: Base58String,
        userPrivateKey: Base58String,
        etheriumAddress: String,
        phoneNumber: PhoneNumber
    ): RegisterWalletResponse = withContext(dispatchers.io) {
        val request = createWalletMapper.toRegisterWalletNetwork(
            userPublicKey = userPublicKey,
            userPrivateKey = userPrivateKey,
            etheriumAddress = etheriumAddress,
            phoneNumber = phoneNumber.e164Formatted(),
            channel = OtpMethod.SMS
        )
        val response = api.registerWallet(request)
        createWalletMapper.fromNetwork(response)
    }

    override suspend fun confirmRegisterWallet(
        userPublicKey: Base58String,
        userPrivateKey: Base58String,
        etheriumAddress: String,
        thirdShare: Web3AuthSignUpResponse.ShareDetailsWithMeta,
        jsonEncryptedMnemonicPhrase: JsonObject,
        phoneNumber: PhoneNumber,
        userSeedPhrase: List<String>,
        socialShareOwnerId: String,
        otpConfirmationCode: String
    ): GatewayServiceStandardResponse = withContext(dispatchers.io) {
        val request = createWalletMapper.toConfirmRegisterWalletNetwork(
            userPublicKey = userPublicKey,
            userPrivateKey = userPrivateKey,
            etheriumAddress = etheriumAddress,
            thirdShare = thirdShare,
            jsonEncryptedMnemonicPhrase = jsonEncryptedMnemonicPhrase,
            phoneNumber = phoneNumber,
            otpConfirmationCode = otpConfirmationCode,
            socialShareOwnerId = socialShareOwnerId,
            userSeedPhrase = userSeedPhrase
        )
        val response = api.confirmRegisterWallet(request)
        createWalletMapper.fromNetwork(response)
    }

    override suspend fun restoreWallet(
        solanaPublicKey: Base58String,
        solanaPrivateKey: Base58String,
        phoneNumber: PhoneNumber,
        channel: OtpMethod
    ): GatewayServiceStandardResponse = withContext(dispatchers.io) {
        val request = restoreWalletMapper.toRestoreWalletNetwork(
            userPublicKey = solanaPublicKey,
            userPrivateKey = solanaPrivateKey,
            phoneNumber = phoneNumber.e164Formatted(),
            channel = channel
        )
        val response = api.restoreWallet(request)
        restoreWalletPublicKey = solanaPublicKey
        createWalletMapper.fromNetwork(response)
    }

    override suspend fun confirmRestoreWallet(
        solanaPublicKey: Base58String,
        solanaPrivateKey: Base58String,
        phoneNumber: PhoneNumber,
        otpConfirmationCode: String
    ): ConfirmRestoreWalletResponse = withContext(dispatchers.io) {
        if (solanaPublicKey != restoreWalletPublicKey) {
            throw RestoreWalletPublicKeyError(
                expectedPublicKey = restoreWalletPublicKey?.base58Value,
                actualPublicKey = solanaPublicKey.base58Value
            ).also { Timber.i(it) }
        }

        val request = restoreWalletMapper.toConfirmRestoreWalletNetwork(
            userPublicKey = solanaPublicKey,
            userPrivateKey = solanaPrivateKey,
            phoneNumber = phoneNumber.e164Formatted(),
            otpConfirmationCode = otpConfirmationCode
        )
        val response = api.confirmRestoreWallet(request)
        createWalletMapper.fromNetwork(response)
            .also { resetTemporaryPublicKeyTimer.stopTimer() }
    }

    override suspend fun loadOnboardingMetadata(
        solanaPublicKey: Base58String,
        solanaPrivateKey: Base58String,
        userSeedPhrase: List<String>,
        etheriumAddress: String,
    ): GatewayOnboardingMetadata = withContext(dispatchers.io) {
        val request = getOnboardingMetadataMapper.toNetwork(
            userPublicKey = solanaPublicKey,
            userPrivateKey = solanaPrivateKey,
            etheriumAddress = etheriumAddress
        )

        val response = api.getOnboardingMetadata(request)
        val metadataFromService = createWalletMapper.fromNetwork(response).onboardingMetadata

        val decryptedMetadata = getOnboardingMetadataMapper.fromNetwork(
            userSeedPhrase = userSeedPhrase,
            metadataCipheredFromService = metadataFromService
        )
        decryptedMetadata
    }

    override suspend fun updateMetadata(
        ethereumAddress: String,
        solanaPublicKey: Base58String,
        solanaPrivateKey: Base58String,
        userSeedPhrase: List<String>,
        metadata: GatewayOnboardingMetadata
    ): UpdateMetadataResponse = withContext(dispatchers.io) {
        val request = updateMetadataMapper.toNetwork(
            ethereumAddress = ethereumAddress,
            userPublicKey = solanaPublicKey,
            userPrivateKey = solanaPrivateKey,
            userSeedPhrase = userSeedPhrase,
            metadata = metadata,
        )
        launch(request).data
    }

    private suspend fun <P, T> launch(request: JsonRpc<P, T>): GatewayResult.Success<T> {
        try {
            val requestGson = gson.toJson(request)
            val response = rpcApi.launch(gatewayUrl, jsonRpc = requestGson)
            val result = request.parseResponse(response, gson)
            return GatewayResult.Success(result)
        } catch (e: JsonRpc.ResponseError.RpcError) {
            Timber.tag(TAG).i(e, "failed request for ${request.method}")
            Timber.tag(TAG).i("Error body message ${e.error.message}")
            throw errorMapper.fromNetwork(e.error)
        }
    }
}
