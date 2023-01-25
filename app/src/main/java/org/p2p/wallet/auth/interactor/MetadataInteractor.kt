package org.p2p.wallet.auth.interactor

import org.p2p.solanaj.core.Account
import org.p2p.wallet.auth.gateway.repository.GatewayServiceRepository
import org.p2p.wallet.auth.repository.UserSignUpDetailsStorage
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseProvider
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.utils.toBase58Instance
import timber.log.Timber
import kotlinx.coroutines.CancellationException

class MetadataInteractor(
    private val gatewayServiceRepository: GatewayServiceRepository,
    private val signUpDetailsStorage: UserSignUpDetailsStorage,
    private val tokenKeyProvider: TokenKeyProvider,
    private val seedPhraseProvider: SeedPhraseProvider,
) {

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

    private suspend fun tryLoadAndSaveMetadataWithAccount(
        userAccount: Account?,
        mnemonicPhraseWords: List<String>,
        ethereumPublicKey: String
    ) {
        try {
            if (userAccount == null) {
                throw GetOnboardingMetadataFailed.GetOnboardingMetadataNoAccount()
            }
            if (mnemonicPhraseWords.isEmpty()) {
                throw GetOnboardingMetadataFailed.GetOnboardingMetadataNoSeedPhrase()
            }
            gatewayServiceRepository.loadAndSaveOnboardingMetadata(
                solanaPublicKey = userAccount.publicKey.toBase58Instance(),
                solanaPrivateKey = userAccount.keypair.toBase58Instance(),
                userSeedPhrase = mnemonicPhraseWords,
                etheriumAddress = ethereumPublicKey
            )
        } catch (cancelled: CancellationException) {
            Timber.i(cancelled)
        } catch (validationError: GetOnboardingMetadataFailed) {
            Timber.e(validationError)
        } catch (error: Throwable) {
            Timber.e(GetOnboardingMetadataFailed.OnboardingMetadataRequestFailure(error))
        }
    }
}
