package org.p2p.wallet.user.interactor

import com.google.gson.Gson
import timber.log.Timber
import org.p2p.core.token.TokenMetadata
import org.p2p.core.token.TokensMetadataInfo
import org.p2p.token.service.model.UpdateTokenMetadataResult
import org.p2p.token.service.repository.metadata.TokenMetadataRepository
import org.p2p.wallet.common.storage.ExternalStorageRepository
import org.p2p.wallet.user.repository.UserLocalRepository

private const val TAG = "TokenMetadataInteractor"
private const val TOKENS_FILE_NAME = "tokens_metadata"

class TokenMetadataInteractor(
    private val externalStorageRepository: ExternalStorageRepository,
    private val userLocalRepository: UserLocalRepository,
    private val metadataRepository: TokenMetadataRepository,
    private val gson: Gson
) {

    suspend fun loadAllTokensData() {
        val file = externalStorageRepository.readJsonFile(filePrefix = TOKENS_FILE_NAME)
        val metadata: TokensMetadataInfo? = if (file != null) {
            gson.fromJson(file.data, TokensMetadataInfo::class.java)
        } else {
            null
        }

        val ifModifiedSince = metadata?.timestamp
        Timber.tag(TAG).i("Checking if metadata is modified since: $ifModifiedSince")

        when (val result = metadataRepository.loadTokensMetadata(ifModifiedSince = ifModifiedSince)) {
            is UpdateTokenMetadataResult.NewMetadata -> updateMemoryCacheAndLocalFile(result)
            is UpdateTokenMetadataResult.NoUpdate -> updateMemoryCache(metadata?.tokens)
            is UpdateTokenMetadataResult.Error -> Timber.tag(TAG).e(result.throwable, "Error loading metadata")
        }
    }

    private fun updateMemoryCacheAndLocalFile(result: UpdateTokenMetadataResult.NewMetadata) {
        val tokensMetadata = result.tokensMetadataInfo

        val lastModified = tokensMetadata.timestamp
        Timber.tag(TAG).i("New tokens metadata received from: $lastModified, updating local storage.")

        userLocalRepository.setTokenData(tokensMetadata.tokens)

        // Save tokens to the file
        externalStorageRepository.saveJson(
            jsonObject = tokensMetadata,
            fileName = TOKENS_FILE_NAME
        )
    }

    private fun updateMemoryCache(tokensMetadata: List<TokenMetadata>?) {
        Timber.tag(TAG).i("Metadata is up-to-date. Using local file")

        if (tokensMetadata == null) {
            Timber.tag(TAG).e("Local file not found!")
            return
        }

        userLocalRepository.setTokenData(tokensMetadata)
    }
}
