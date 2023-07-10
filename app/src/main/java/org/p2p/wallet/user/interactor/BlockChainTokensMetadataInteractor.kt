package org.p2p.wallet.user.interactor

import com.google.gson.Gson
import timber.log.Timber
import org.p2p.core.token.TokenData
import org.p2p.wallet.common.feature_toggles.toggles.remote.TokenMetadataUpdateFeatureToggle
import org.p2p.wallet.common.storage.ExternalStorageRepository
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.user.repository.UserRepository

private const val TAG = "BlockChainTokensInteractor"

class BlockChainTokensMetadataInteractor(
    private val metadataUpdateFeatureToggle: TokenMetadataUpdateFeatureToggle,
    private val externalStorageRepository: ExternalStorageRepository,
    private val userLocalRepository: UserLocalRepository,
    private val userRepository: UserRepository,
    private val gson: Gson
) {

    suspend fun loadAllTokensData() {
        val file = externalStorageRepository.readJsonFile(TOKENS_FILE_NAME)

        if (!metadataUpdateFeatureToggle.isFeatureEnabled && file != null) {
            Timber.tag(TAG).i("Tokens data file was found. Trying to parse it...")
            val tokens = gson.fromJson(file.data, Array<TokenData>::class.java)?.toList()
            if (tokens != null) {
                Timber.tag(TAG).i("Tokens data were successfully parsed from file.")
                userLocalRepository.setTokenData(tokens)
                return
            }
        }

        Timber.tag(TAG).i("Tokens data file was not found. Loading from remote")
        // If the file is not found or empty, load from network
        val data = userRepository.loadAllTokens()
        userLocalRepository.setTokenData(data)

        // Save tokens to the file
        externalStorageRepository.saveJson(json = gson.toJson(data), fileName = TOKENS_FILE_NAME)
        return
    }
}
