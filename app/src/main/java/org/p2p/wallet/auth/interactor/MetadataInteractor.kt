package org.p2p.wallet.auth.interactor

import timber.log.Timber
import kotlinx.coroutines.CancellationException
import org.p2p.solanaj.core.Account
import org.p2p.wallet.auth.gateway.repository.GatewayServiceRepository
import org.p2p.wallet.auth.gateway.repository.model.GatewayOnboardingMetadata
import org.p2p.wallet.auth.repository.UserSignUpDetailsStorage
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseProvider
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.infrastructure.security.SecureStorageContract
import org.p2p.wallet.utils.toBase58Instance

class MetadataInteractor(
    private val gatewayServiceRepository: GatewayServiceRepository,
    private val signUpDetailsStorage: UserSignUpDetailsStorage,
    private val tokenKeyProvider: TokenKeyProvider,
    private val seedPhraseProvider: SeedPhraseProvider,
    private val secureStorageContract: SecureStorageContract,
) {

    var currentMetadata: GatewayOnboardingMetadata? = null

    suspend fun tryLoadAndSaveMetadata() {
        val web3SignUpDetails = signUpDetailsStorage.getLastSignUpUserDetails()?.signUpDetails
        if (web3SignUpDetails != null) {
            val userAccount = Account(tokenKeyProvider.keyPair)
            val userSeedPhrase = seedPhraseProvider.getUserSeedPhrase()
            tryLoadAndSaveMetadataWithAccount(
                userAccount = userAccount,
                mnemonicPhraseWords = userSeedPhrase.seedPhrase,
                ethereumPublicKey = web3SignUpDetails.ethereumPublicKey
            )
        } else {
            Timber.i("User doesn't have any Web3Auth sign up data, skipping metadata fetch")
        }
    }

    suspend fun updateMetadata(metadata: GatewayOnboardingMetadata) {
        currentMetadata = metadata
        tryToUploadMetadata(metadata)
        secureStorageContract.saveObject(SecureStorageContract.Key.KEY_ONBOARDING_METADATA, metadata)
    }

    private suspend fun tryLoadAndSaveMetadataWithAccount(
        userAccount: Account?,
        mnemonicPhraseWords: List<String>,
        ethereumPublicKey: String
    ) {
        try {
            if (userAccount == null) {
                throw MetadataFailed.MetadataNoAccount()
            }
            if (mnemonicPhraseWords.isEmpty()) {
                throw MetadataFailed.MetadataNoSeedPhrase()
            }
            val metadata = gatewayServiceRepository.loadOnboardingMetadata(
                solanaPublicKey = userAccount.publicKey.toBase58Instance(),
                solanaPrivateKey = userAccount.keypair.toBase58Instance(),
                userSeedPhrase = mnemonicPhraseWords,
                etheriumAddress = ethereumPublicKey
            )
            compareMetadataAndUpdate(metadata)
        } catch (cancelled: CancellationException) {
            Timber.i(cancelled)
        } catch (validationError: MetadataFailed) {
            Timber.e(validationError, "Get onboarding metadata failed")
        } catch (error: Throwable) {
            Timber.e(MetadataFailed.OnboardingMetadataRequestFailure(error))
        }
    }

    private suspend fun tryToUploadMetadata(metadata: GatewayOnboardingMetadata) {
        val web3SignUpDetails = signUpDetailsStorage.getLastSignUpUserDetails()?.signUpDetails
        if (web3SignUpDetails != null) {
            val userAccount = Account(tokenKeyProvider.keyPair)
            val userSeedPhrase = seedPhraseProvider.getUserSeedPhrase()
            tryToUploadMetadata(
                userAccount = userAccount,
                mnemonicPhraseWords = userSeedPhrase.seedPhrase,
                ethereumPublicKey = web3SignUpDetails.ethereumPublicKey,
                metadata = metadata
            )
        } else {
            Timber.i("User doesn't have any Web3Auth sign up data, skipping metadata fetch")
        }
    }

    private suspend fun tryToUploadMetadata(
        userAccount: Account?,
        mnemonicPhraseWords: List<String>,
        ethereumPublicKey: String,
        metadata: GatewayOnboardingMetadata
    ) {
        try {
            if (userAccount == null) {
                throw MetadataFailed.MetadataNoAccount()
            }
            if (mnemonicPhraseWords.isEmpty()) {
                throw MetadataFailed.MetadataNoSeedPhrase()
            }
            gatewayServiceRepository.updateMetadata(
                solanaPublicKey = userAccount.publicKey.toBase58Instance(),
                solanaPrivateKey = userAccount.keypair.toBase58Instance(),
                userSeedPhrase = mnemonicPhraseWords,
                ethereumAddress = ethereumPublicKey,
                metadata = metadata
            )
        } catch (cancelled: CancellationException) {
            Timber.i(cancelled)
        } catch (validationError: MetadataFailed) {
            Timber.e(validationError, "Update metadata failed")
        } catch (error: Throwable) {
            Timber.e(MetadataFailed.OnboardingMetadataRequestFailure(error))
        }
    }

    private suspend fun compareMetadataAndUpdate(serverMetadata: GatewayOnboardingMetadata) {
        val finalMetadata = secureStorageContract.getObject(
            SecureStorageContract.Key.KEY_ONBOARDING_METADATA,
            GatewayOnboardingMetadata::class
        )?.let { deviceMetadata ->
            val updatedMetadata = getUpdatedMergedMetadata(deviceMetadata, serverMetadata)
            if (updatedMetadata == serverMetadata) {
                tryToUploadMetadata(updatedMetadata)
            }
            updatedMetadata
        } ?: serverMetadata
        currentMetadata = finalMetadata
        secureStorageContract.saveObject(SecureStorageContract.Key.KEY_ONBOARDING_METADATA, finalMetadata)
    }

    private fun getUpdatedMergedMetadata(
        serverMetadata: GatewayOnboardingMetadata,
        deviceMetadata: GatewayOnboardingMetadata
    ): GatewayOnboardingMetadata {
        return if (serverMetadata.metaTimestampSec < deviceMetadata.metaTimestampSec) {
            deviceMetadata
        } else if (serverMetadata.metaTimestampSec > deviceMetadata.metaTimestampSec) {
            serverMetadata
        } else {
            val updatedDeviceNamePair = getNewerValue(
                deviceMetadata.deviceNameTimestampSec,
                serverMetadata.deviceNameTimestampSec,
                deviceMetadata.deviceShareDeviceName,
                serverMetadata.deviceShareDeviceName
            )
            val updatedPhoneNumberPair = getNewerValue(
                deviceMetadata.phoneNumberTimestampSec,
                serverMetadata.phoneNumberTimestampSec,
                deviceMetadata.customSharePhoneNumberE164,
                serverMetadata.customSharePhoneNumberE164
            )
            val updatedEmailPair = getNewerValue(
                deviceMetadata.emailTimestampSec,
                serverMetadata.emailTimestampSec,
                deviceMetadata.socialShareOwnerEmail,
                serverMetadata.socialShareOwnerEmail
            )
            val updatedStrigaMetadata =
                if (deviceMetadata.strigaMetadata != null && serverMetadata.strigaMetadata != null) {
                    getNewerValue(
                        deviceMetadata.strigaMetadata.userIdTimestamp,
                        serverMetadata.strigaMetadata.userIdTimestamp,
                        deviceMetadata.strigaMetadata,
                        serverMetadata.strigaMetadata
                    ).first
                } else {
                    deviceMetadata.strigaMetadata ?: serverMetadata.strigaMetadata
                }

            deviceMetadata.copy(
                ethPublic = deviceMetadata.ethPublic ?: serverMetadata.ethPublic,
                deviceShareDeviceName = updatedDeviceNamePair.first,
                deviceNameTimestampSec = updatedDeviceNamePair.second,
                customSharePhoneNumberE164 = updatedPhoneNumberPair.first,
                phoneNumberTimestampSec = updatedPhoneNumberPair.second,
                socialShareOwnerEmail = updatedEmailPair.first,
                emailTimestampSec = updatedEmailPair.second,
                strigaMetadata = updatedStrigaMetadata
            )
        }
    }

    private fun <T> getNewerValue(
        firstMetaValueTimestamp: Long,
        secondMetaValueTimestamp: Long,
        firstValue: T,
        secondValue: T,
    ): Pair<T, Long> {
        return if (secondMetaValueTimestamp > firstMetaValueTimestamp) {
            secondValue to secondMetaValueTimestamp
        } else {
            firstValue to firstMetaValueTimestamp
        }
    }
}
