package org.p2p.wallet.user.interactor

import com.google.gson.Gson
import timber.log.Timber
import org.p2p.core.token.TokenMetadata
import org.p2p.token.service.model.TokenMetadataResult
import org.p2p.token.service.repository.metadata.TokenMetadataRepository
import org.p2p.wallet.common.storage.ExternalFile
import org.p2p.wallet.common.storage.ExternalStorageRepository
import org.p2p.wallet.user.repository.UserLocalRepository

private const val TAG = "TokenMetadataInteractor"
private const val TOKENS_FILE_NAME = "tokens.json"

class TokenMetadataInteractor(
    private val externalStorageRepository: ExternalStorageRepository,
    private val userLocalRepository: UserLocalRepository,
    private val metadataRepository: TokenMetadataRepository,
    private val gson: Gson
) {

    suspend fun loadAllTokensData() {
        val file = externalStorageRepository.readJsonFile(TOKENS_FILE_NAME)

        val lastModified = if (file != null) {
            gson.fromJson(file.data, TokenMetadata::class.java).timestamp
        } else {
            null
        }

        Timber.tag(TAG).i("Checking if metadata is modified since: $lastModified")

        when (val result = metadataRepository.loadTokensMetadata(lastModified = lastModified)) {
            is TokenMetadataResult.NewMetadata -> updateMemoryCacheAndLocalFile(result)
            is TokenMetadataResult.NoUpdate -> updateMemoryCache(file)
            is TokenMetadataResult.Error -> Timber.tag(TAG).e(result.throwable, "Error loading metadata")
        }
    }

    private fun updateMemoryCacheAndLocalFile(result: TokenMetadataResult.NewMetadata) {
        Timber.tag(TAG).i("Received an updated tokens metadata, updating file in local storage")

        val tokensMetadata = result.tokensMetadata
        userLocalRepository.setTokenData(tokensMetadata.data)

        // Save tokens to the file
        externalStorageRepository.saveJson(
            json = gson.toJson(tokensMetadata),
            fileName = TOKENS_FILE_NAME
        )
    }

    private fun updateMemoryCache(file: ExternalFile?) {
        Timber.tag(TAG).i("Metadata is up-to-date. Using local file")

        if (file == null) {
            Timber.tag(TAG).e("Local file not found!")
            return
        }

        val tokensMetadata = gson.fromJson(file.data, TokenMetadata::class.java)
        if (tokensMetadata != null) {
            Timber.tag(TAG).i("Tokens data were successfully parsed from file.")
            userLocalRepository.setTokenData(tokensMetadata.data)
        }
    }
}
