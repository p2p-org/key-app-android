package org.p2p.wallet.auth.interactor

import timber.log.Timber
import kotlinx.coroutines.CancellationException
import org.p2p.core.crypto.toBase58Instance
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.toBase58Instance
import org.p2p.wallet.auth.gateway.repository.GatewayServiceRepository
import org.p2p.wallet.auth.gateway.repository.model.GatewayOnboardingMetadata
import org.p2p.wallet.auth.model.MetadataLoadStatus
import org.p2p.wallet.auth.repository.RestoreFlowDataLocalRepository
import org.p2p.wallet.auth.repository.UserSignUpDetailsStorage
import org.p2p.wallet.auth.web3authsdk.Web3AuthApi
import org.p2p.wallet.bridge.interactor.EthereumInteractor
import org.p2p.wallet.common.feature_toggles.toggles.remote.EthAddressEnabledFeatureToggle
import org.p2p.wallet.infrastructure.account.AccountStorageContract
import org.p2p.wallet.infrastructure.account.AccountStorageContract.Key.Companion.withCustomKey
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseProvider
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseSource
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.settings.DeviceInfoHelper

private const val TAG = "MetadataInteractor"

class MetadataInteractor(
    private val gatewayServiceRepository: GatewayServiceRepository,
    private val signUpDetailsStorage: UserSignUpDetailsStorage,
    private val tokenKeyProvider: TokenKeyProvider,
    private val seedPhraseProvider: SeedPhraseProvider,
    private val accountStorage: AccountStorageContract,
    private val gatewayMetadataMerger: GatewayMetadataMerger,
    private val ethereumInteractor: EthereumInteractor,
    private val bridgeFeatureToggle: EthAddressEnabledFeatureToggle,
    private val metadataChangesLogger: MetadataChangesLogger,
    private val restoreFlowDataLocalRepository: RestoreFlowDataLocalRepository,
    private val web3AuthApi: Web3AuthApi,
) {

    var currentMetadata: GatewayOnboardingMetadata? = null
        get() = getMetadataFromStorage()
        private set(value) {
            Timber.tag(TAG).i("updating currentMetadata field")
            field = value
            saveMetadataToStorage(value)
        }

    suspend fun tryLoadAndSaveMetadata(): MetadataLoadStatus {
        val ethereumPublicKey = getEthereumPublicKey()
        return if (ethereumPublicKey != null) {
            Timber.tag(TAG).i("ETH key is found, fetching metadata")
            val userAccount = Account(tokenKeyProvider.keyPair)
            val userSeedPhrase = seedPhraseProvider.getUserSeedPhrase().seedPhrase
            loadAndSaveMetadata(
                userAccount = userAccount,
                mnemonicPhraseWords = userSeedPhrase,
                ethereumPublicKey = ethereumPublicKey
            )
        } else {
            Timber.i("User doesn't have ethereum public key, skipping metadata fetch")
            MetadataLoadStatus.NoEthereumPublicKey
        }
    }

    private fun hasDeviceShare(): Boolean {
        val userDetails = signUpDetailsStorage.getLastSignUpUserDetails()
        return (userDetails?.signUpDetails?.deviceShare != null)
            .also { Timber.tag(TAG).i("hasDeviceShare: $it") }
    }

    fun hasDifferentDeviceShare(): Boolean {
        val metadata = currentMetadata ?: return false

        // if device share exist, then we ignore comparing
        if (hasDeviceShare()) {
            return false
        }

        // if device share is not empty we are checking with the current system device share
        return DeviceInfoHelper.getCurrentDeviceName() != metadata.deviceShareDeviceName
    }

    private fun getEthereumPublicKey(): String? {
        return signUpDetailsStorage.getLastSignUpUserDetails()
            ?.signUpDetails
            ?.ethereumPublicKey
            ?: tryToGetEthAddress()
    }

    private fun tryToGetEthAddress(): String? {
        return if (bridgeFeatureToggle.isFeatureEnabled) {
            ethereumInteractor.getEthAddress().hex
        } else {
            null
        }
    }

    private fun saveMetadataToStorage(metadata: GatewayOnboardingMetadata?) {
        // TODO PWN-8771 - implement database for metadata
        val ethAddress = getEthereumPublicKey().orEmpty()
        accountStorage.saveObject(
            key = AccountStorageContract.Key.KEY_ONBOARDING_METADATA.withCustomKey(ethAddress),
            data = metadata
        )
    }

    private fun getMetadataFromStorage(): GatewayOnboardingMetadata? {
        if (seedPhraseProvider.getUserSeedPhrase().provider != SeedPhraseSource.WEB_AUTH) {
            return null
        }
        // TODO PWN-8771 - implement database for metadata
        val ethAddress = getEthereumPublicKey().orEmpty()
        return accountStorage.getObject(
            AccountStorageContract.Key.KEY_ONBOARDING_METADATA.withCustomKey(ethAddress),
            GatewayOnboardingMetadata::class
        )
    }

    suspend fun updateMetadata(metadata: GatewayOnboardingMetadata) {
        metadataChangesLogger.logChange(
            metadataOld = currentMetadata,
            metadataNew = metadata
        )
        currentMetadata = metadata
        tryToUploadMetadataOnServer(metadata)
    }

    private suspend fun loadAndSaveMetadata(
        userAccount: Account?,
        mnemonicPhraseWords: List<String>,
        ethereumPublicKey: String
    ): MetadataLoadStatus {
        return try {
            if (userAccount == null) {
                throw MetadataFailed.MetadataNoAccount()
            }
            if (mnemonicPhraseWords.isEmpty()) {
                throw MetadataFailed.MetadataNoSeedPhrase()
            }
            val serverMetadata = gatewayServiceRepository.loadOnboardingMetadata(
                solanaPublicKey = userAccount.publicKey.toBase58Instance(),
                solanaPrivateKey = userAccount.keypair.toBase58Instance(),
                userSeedPhrase = mnemonicPhraseWords,
                etheriumAddress = ethereumPublicKey
            )
            val torusMetadata = getTorusMetadata()
            compareMetadataAndSave(serverMetadata, torusMetadata)
            MetadataLoadStatus.Success
        } catch (cancelled: CancellationException) {
            Timber.i(cancelled)
            MetadataLoadStatus.Canceled
        } catch (validationError: MetadataFailed) {
            Timber.e(validationError, "Get onboarding metadata failed")
            MetadataLoadStatus.Failure(validationError)
        } catch (error: Throwable) {
            val targetError = MetadataFailed.OnboardingMetadataRequestFailure(error)
            Timber.e(targetError)
            MetadataLoadStatus.Failure(targetError)
        }
    }

    private suspend fun getTorusMetadata(): GatewayOnboardingMetadata? {
        if (!restoreFlowDataLocalRepository.isTorusKeyValid()) {
            return null
        }
        return try {
            web3AuthApi.getUserData()
        } catch (error: Throwable) {
            Timber.e(error)
            null
        }
    }

    private suspend fun tryToUploadMetadataOnTorus(metadata: GatewayOnboardingMetadata) {
        if (restoreFlowDataLocalRepository.isTorusKeyValid()) {
            try {
                web3AuthApi.setUserData(metadata)
            } catch (error: Throwable) {
                Timber.e(error)
            }
        }
    }

    private suspend fun tryToUploadMetadataOnServer(metadata: GatewayOnboardingMetadata) {
        val ethereumPublicKey = getEthereumPublicKey()
        if (ethereumPublicKey != null) {
            Timber.tag(TAG).i("uploading new metadata")
            val userAccount = Account(tokenKeyProvider.keyPair)
            val userSeedPhrase = seedPhraseProvider.getUserSeedPhrase().seedPhrase
            tryToUploadMetadataOnServer(
                userAccount = userAccount,
                mnemonicPhraseWords = userSeedPhrase,
                ethereumPublicKey = ethereumPublicKey,
                newMetadata = metadata
            )
        } else {
            Timber.tag(TAG).i("User doesn't have any Web3Auth sign up data, skipping upload metadata")
        }
    }

    private suspend fun tryToUploadMetadataOnServer(
        userAccount: Account?,
        mnemonicPhraseWords: List<String>,
        ethereumPublicKey: String,
        newMetadata: GatewayOnboardingMetadata
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
                metadata = newMetadata
            )
        } catch (cancelled: CancellationException) {
            Timber.i(cancelled)
        } catch (validationError: MetadataFailed) {
            Timber.e(validationError, "Update metadata failed")
        } catch (error: Throwable) {
            Timber.e(MetadataFailed.OnboardingMetadataRequestFailure(error))
        }
    }

    private suspend fun compareMetadataAndSave(
        serverMetadata: GatewayOnboardingMetadata,
        torusMetadata: GatewayOnboardingMetadata?
    ) {
        val finalMetadata = currentMetadata?.let { deviceMetadata ->
            val mergedWithServerMetadata = gatewayMetadataMerger.merge(serverMetadata, deviceMetadata)
            val updatedMetadata = if (torusMetadata == null) {
                mergedWithServerMetadata
            } else {
                gatewayMetadataMerger.merge(torusMetadata, mergedWithServerMetadata)
            }
            if (updatedMetadata != serverMetadata) {
                tryToUploadMetadataOnServer(updatedMetadata)
                tryToUploadMetadataOnTorus(updatedMetadata)
            }
            updatedMetadata
        } ?: serverMetadata
        metadataChangesLogger.logChange(
            metadataOld = currentMetadata,
            metadataNew = finalMetadata
        )
        currentMetadata = finalMetadata
    }
}
