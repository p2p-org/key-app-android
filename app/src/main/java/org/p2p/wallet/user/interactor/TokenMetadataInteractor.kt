package org.p2p.wallet.user.interactor

import com.google.gson.Gson
import timber.log.Timber
import org.p2p.core.token.TokensMetadataInfo
import org.p2p.token.service.model.UpdateTokenMetadataResult
import org.p2p.token.service.repository.metadata.TokenMetadataRepository
import org.p2p.wallet.common.storage.ExternalFile
import org.p2p.wallet.common.storage.ExternalStorageRepository
import org.p2p.wallet.user.repository.UserLocalRepository

private const val TAG = "TokenMetadataInteractor"
private const val TOKENS_FILE_NAME = "tokens"

class TokenMetadataInteractor(
    private val externalStorageRepository: ExternalStorageRepository,
    private val userLocalRepository: UserLocalRepository,
    private val metadataRepository: TokenMetadataRepository,
    private val gson: Gson
) {

    suspend fun loadAllTokensData() {
        val file = externalStorageRepository.readJsonFile(TOKENS_FILE_NAME)

        val lastModified = if (file != null) {
            gson.fromJson(file.data, TokensMetadataInfo::class.java).timestamp
        } else {
            null
        }

        Timber.tag(TAG).i("Checking if metadata is modified since: $lastModified")

        when (val result = metadataRepository.loadTokensMetadata(lastModified = lastModified)) {
            is UpdateTokenMetadataResult.NewMetadata -> updateMemoryCacheAndLocalFile(result)
            is UpdateTokenMetadataResult.NoUpdate -> updateMemoryCache(file)
            is UpdateTokenMetadataResult.Error -> Timber.tag(TAG).e(result.throwable, "Error loading metadata")
        }
    }

    private fun updateMemoryCacheAndLocalFile(result: UpdateTokenMetadataResult.NewMetadata) {
        Timber.tag(TAG).i("Received an updated tokens metadata, updating file in local storage")

        val tokensMetadata = result.tokensMetadataInfo
        userLocalRepository.setTokenData(tokensMetadata.tokens)

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

        val tokensMetadataInfo = gson.fromJson(file.data, TokensMetadataInfo::class.java)
        if (tokensMetadataInfo != null) {
            Timber.tag(TAG).i("Tokens data were successfully parsed from file.")
            userLocalRepository.setTokenData(tokensMetadataInfo.tokens)
        }
    }
}
