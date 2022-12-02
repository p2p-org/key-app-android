package org.p2p.wallet.auth.gateway.repository

import com.google.gson.JsonObject
import org.p2p.wallet.auth.gateway.api.GatewayServiceApi
import org.p2p.wallet.auth.gateway.api.request.OtpMethod
import org.p2p.wallet.auth.gateway.api.response.ConfirmRestoreWalletResponse
import org.p2p.wallet.auth.gateway.api.response.GatewayServiceStandardResponse
import org.p2p.wallet.auth.gateway.api.response.RegisterWalletResponse
import org.p2p.wallet.auth.gateway.repository.mapper.GatewayServiceCreateWalletMapper
import org.p2p.wallet.auth.gateway.repository.mapper.GatewayServiceGetOnboardingMetadataMapper
import org.p2p.wallet.auth.gateway.repository.mapper.GatewayServiceRestoreWalletMapper
import org.p2p.wallet.auth.gateway.repository.model.RestoreWalletPublicKeyError
import org.p2p.wallet.auth.model.PhoneNumber
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthSignUpResponse
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.infrastructure.security.SecureStorageContract
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.FlowDurationTimer
import timber.log.Timber
import kotlin.time.DurationUnit
import kotlinx.coroutines.withContext

class GatewayServiceRemoteRepository(
    private val api: GatewayServiceApi,
    private val createWalletMapper: GatewayServiceCreateWalletMapper,
    private val restoreWalletMapper: GatewayServiceRestoreWalletMapper,
    private val getOnboardingMetadataMapper: GatewayServiceGetOnboardingMetadataMapper,
    private val secureStorageContract: SecureStorageContract,
    private val dispatchers: CoroutineDispatchers,
    private val appScope: AppScope
) : GatewayServiceRepository {

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

    override suspend fun loadAndSaveOnboardingMetadata(
        solanaPublicKey: Base58String,
        solanaPrivateKey: Base58String,
        userSeedPhrase: List<String>,
        etheriumAddress: String,
    ) = withContext(dispatchers.io) {
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
        secureStorageContract.saveObject(SecureStorageContract.Key.KEY_ONBOARDING_METADATA, decryptedMetadata)
    }
}
