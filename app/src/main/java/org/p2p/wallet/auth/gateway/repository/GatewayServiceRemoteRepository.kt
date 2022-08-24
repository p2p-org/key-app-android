package org.p2p.wallet.auth.gateway.repository

import com.google.gson.JsonObject
import org.p2p.wallet.auth.gateway.api.GatewayServiceApi
import org.p2p.wallet.auth.gateway.api.request.OtpMethod
import org.p2p.wallet.auth.gateway.api.response.ConfirmRestoreWalletResponse
import org.p2p.wallet.auth.gateway.api.response.GatewayServiceStandardResponse
import org.p2p.wallet.auth.gateway.api.response.RegisterWalletResponse
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthSignUpResponse.ShareDetailsWithMeta.ShareInnerDetails.ShareValue
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.FlowDurationTimer
import timber.log.Timber
import kotlin.time.DurationUnit
import kotlinx.coroutines.withContext

class GatewayServiceRemoteRepository(
    private val api: GatewayServiceApi,
    private val createWalletMapper: GatewayServiceCreateWalletMapper,
    private val restoreWalletMapper: GatewayServiceRestoreWalletMapper,
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
            resetTemporaryPublicKeyTimer.launchTimer(appScope)
        }

    override suspend fun registerWalletWithSms(
        userPublicKey: Base58String,
        userPrivateKey: Base58String,
        etheriumPublicKey: String,
        phoneNumber: String
    ): RegisterWalletResponse = withContext(dispatchers.io) {
        val request = createWalletMapper.toRegisterWalletNetwork(
            userPublicKey = userPublicKey,
            userPrivateKey = userPrivateKey,
            etheriumPublicKey = etheriumPublicKey,
            phoneNumber = phoneNumber,
            channel = OtpMethod.SMS
        )
        val response = api.registerWallet(request)
        createWalletMapper.fromNetwork(response)
    }

    override suspend fun confirmRegisterWallet(
        userPublicKey: Base58String,
        userPrivateKey: Base58String,
        etheriumPublicKey: String,
        thirdShare: ShareValue,
        jsonEncryptedMnemonicPhrase: JsonObject,
        phoneNumber: String,
        otpConfirmationCode: String
    ): GatewayServiceStandardResponse = withContext(dispatchers.io) {
        val request = createWalletMapper.toConfirmRegisterWalletNetwork(
            userPublicKey = userPublicKey,
            userPrivateKey = userPrivateKey,
            etheriumPublicKey = etheriumPublicKey,
            thirdShare = thirdShare,
            jsonEncryptedMnemonicPhrase = jsonEncryptedMnemonicPhrase,
            phoneNumber = phoneNumber,
            otpConfirmationCode = otpConfirmationCode
        )
        val response = api.confirmRegisterWallet(request)
        createWalletMapper.fromNetwork(response)
    }

    override suspend fun restoreWallet(
        solanaPublicKey: Base58String,
        solanaPrivateKey: Base58String,
        phoneNumber: String,
        channel: OtpMethod
    ): GatewayServiceStandardResponse = withContext(dispatchers.io) {
        val request = restoreWalletMapper.toRestoreWalletNetwork(
            userPublicKey = solanaPublicKey,
            userPrivateKey = solanaPrivateKey,
            phoneNumber = phoneNumber,
            channel = channel
        )
        val response = api.restoreWallet(request)
        restoreWalletPublicKey = solanaPublicKey
        createWalletMapper.fromNetwork(response)
    }

    override suspend fun confirmRestoreWallet(
        solanaPublicKey: Base58String,
        solanaPrivateKey: Base58String,
        phoneNumber: String,
        otpConfirmationCode: String
    ): ConfirmRestoreWalletResponse = withContext(dispatchers.io) {
        if (solanaPublicKey != restoreWalletPublicKey) {
            throw RestoreWalletPublicKeyError(
                expectedPublicKey = restoreWalletPublicKey?.base58Value,
                actualPublicKey = solanaPublicKey.base58Value
            )
                .also { Timber.i(it) }
        }

        val request = restoreWalletMapper.toConfirmRestoreWalletNetwork(
            userPublicKey = solanaPublicKey,
            userPrivateKey = solanaPrivateKey,
            phoneNumber = phoneNumber,
            otpConfirmationCode = otpConfirmationCode
        )
        val response = api.confirmRestoreWallet(request)
        createWalletMapper.fromNetwork(response)
            .also { resetTemporaryPublicKeyTimer.stopTimer() }
    }
}
