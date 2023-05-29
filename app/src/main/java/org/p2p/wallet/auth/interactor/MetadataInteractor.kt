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
    private val gatewayMetadataMerger: GatewayMetadataMerger,
) {

    var currentMetadata: GatewayOnboardingMetadata? = secureStorageContract.getObject(
        SecureStorageContract.Key.KEY_ONBOARDING_METADATA,
        GatewayOnboardingMetadata::class
    )

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
                newMetadata = metadata
            )
        } else {
            Timber.i("User doesn't have any Web3Auth sign up data, skipping upload metadata")
        }
    }

    private suspend fun tryToUploadMetadata(
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

    private suspend fun compareMetadataAndUpdate(serverMetadata: GatewayOnboardingMetadata) {
        val finalMetadata = currentMetadata?.let { deviceMetadata ->
            val updatedMetadata = gatewayMetadataMerger.merge(deviceMetadata, serverMetadata)
            if (updatedMetadata != serverMetadata) {
                tryToUploadMetadata(updatedMetadata)
            }
            updatedMetadata
        } ?: serverMetadata
        currentMetadata = finalMetadata
        secureStorageContract.saveObject(SecureStorageContract.Key.KEY_ONBOARDING_METADATA, finalMetadata)
    }
}
