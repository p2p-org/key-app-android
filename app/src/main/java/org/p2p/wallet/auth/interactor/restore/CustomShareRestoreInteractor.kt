package org.p2p.wallet.auth.interactor.restore

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.p2p.solanaj.utils.crypto.decodeFromBase64
import org.p2p.wallet.auth.gateway.api.request.OtpMethod
import org.p2p.wallet.auth.gateway.api.response.ConfirmRestoreWalletResponse
import org.p2p.wallet.auth.gateway.repository.GatewayServiceRepository
import org.p2p.wallet.auth.model.PhoneNumber
import org.p2p.wallet.auth.model.RestoreWalletFailure
import org.p2p.wallet.auth.repository.RestoreFlowDataLocalRepository
import org.p2p.wallet.auth.repository.UserSignUpDetailsStorage
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthSignUpResponse
import org.p2p.wallet.utils.fromJsonReified
import timber.log.Timber

class CustomShareRestoreInteractor(
    private val gatewayServiceRepository: GatewayServiceRepository,
    private val restoreFlowDataLocalRepository: RestoreFlowDataLocalRepository,
    private val signUpDetailsStorage: UserSignUpDetailsStorage,
    private val gson: Gson,
) {

    suspend fun startRestoreCustomShare(userPhoneNumber: PhoneNumber, isResend: Boolean = false) {
        val temporaryUserPublicKey = restoreFlowDataLocalRepository.userRestorePublicKey
            ?: throw RestoreWalletFailure("User restore public key is null")
        val temporaryUserPrivateKey = restoreFlowDataLocalRepository.userRestorePrivateKey
            ?: throw RestoreWalletFailure("User restore private key is null")

        if (isResend || userPhoneNumber != restoreFlowDataLocalRepository.userPhoneNumber) {
            gatewayServiceRepository.restoreWallet(
                solanaPublicKey = temporaryUserPublicKey,
                solanaPrivateKey = temporaryUserPrivateKey,
                phoneNumber = userPhoneNumber,
                channel = OtpMethod.SMS
            )
        }
        restoreFlowDataLocalRepository.userPhoneNumber = userPhoneNumber
    }

    suspend fun finishRestoreCustomShare(smsCode: String) {
        val temporaryUserPublicKey = restoreFlowDataLocalRepository.userRestorePublicKey
            ?: throw RestoreWalletFailure("User restore public key is null")
        val temporaryUserPrivateKey = restoreFlowDataLocalRepository.userRestorePrivateKey
            ?: throw RestoreWalletFailure("User restore private key is null")
        val userPhoneNumber = restoreFlowDataLocalRepository.userPhoneNumber
            ?: throw RestoreWalletFailure("User restore phone number is null")

        val result: ConfirmRestoreWalletResponse = gatewayServiceRepository.confirmRestoreWallet(
            solanaPublicKey = temporaryUserPublicKey,
            solanaPrivateKey = temporaryUserPrivateKey,
            phoneNumber = userPhoneNumber,
            otpConfirmationCode = smsCode
        )

        restoreFlowDataLocalRepository.apply {
            customShare = convertBase64ToShareWithMeta(result.thirdShareStructBase64)
            encryptedMnemonic = convertBase64ToEncryptedMnemonics(result.encryptedMnemonicsStructBase64).toString()
            deviceShare = signUpDetailsStorage.getLastSignUpUserDetails()?.signUpDetails?.deviceShare
        }
    }

    private fun convertBase64ToShareWithMeta(
        thirdShareStructAsBase64: String
    ): Web3AuthSignUpResponse.ShareDetailsWithMeta {
        val thirdShareJson = String(thirdShareStructAsBase64.decodeFromBase64())
        return gson.fromJsonReified<Web3AuthSignUpResponse.ShareDetailsWithMeta>(thirdShareJson)
            ?: run {
                Timber.i(thirdShareStructAsBase64)
                Timber.i(thirdShareJson)
                throw RestoreWalletFailure("Couldn't convert base64 to third share")
            }
    }

    private fun convertBase64ToEncryptedMnemonics(
        encryptedMnemonicsStruct: String
    ): JsonObject {
        val encryptedMnemonicsJson = String(encryptedMnemonicsStruct.decodeFromBase64())
        return gson.fromJsonReified<JsonObject>(encryptedMnemonicsJson)
            ?: run {
                Timber.i(encryptedMnemonicsStruct)
                Timber.i(encryptedMnemonicsJson)
                throw RestoreWalletFailure("Couldn't convert base64 to encrypted mnemonics")
            }
    }
}
