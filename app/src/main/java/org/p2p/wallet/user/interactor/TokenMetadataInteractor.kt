package org.p2p.wallet.user.interactor

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import timber.log.Timber
import org.p2p.core.token.TokensMetadataInfo
import org.p2p.token.service.model.UpdateTokenMetadataResult
import org.p2p.token.service.repository.metadata.TokenMetadataRepository
import org.p2p.wallet.common.storage.ExternalStorageRepository
import org.p2p.wallet.user.repository.UserLocalRepository

private const val TAG = "TokenMetadataInteractor"
const val TOKENS_FILE_NAME = "token_service_metadata"

class TokenMetadataInteractor(
    private val externalStorageRepository: ExternalStorageRepository,
    private val userLocalRepository: UserLocalRepository,
    private val metadataRepository: TokenMetadataRepository,
    private val gson: Gson
) {

    suspend fun loadAllTokensMetadata() {
        val metadataFromFile: TokensMetadataInfo? = readTokensMetadataFromFile()

        val modifiedSince = metadataFromFile?.timestamp
        Timber.tag(TAG).i("Checking if metadata is modified since: $modifiedSince")

        when (val result = metadataRepository.loadTokensMetadata(ifModifiedSince = modifiedSince)) {
            is UpdateTokenMetadataResult.NewMetadata -> {
                updateLocalFile(result.remoteTokensMetadata)
                updateMemoryCache(result.remoteTokensMetadata)
            }
            is UpdateTokenMetadataResult.NoUpdate -> {
                updateMemoryCache(metadataFromFile)
            }
            is UpdateTokenMetadataResult.Error -> {
                handleError(result.throwable)
            }
        }
    }

    private suspend fun readTokensMetadataFromFile(): TokensMetadataInfo? {
        return try {
            Timber.tag("____").d("Try to read metadata")
            val file = externalStorageRepository.readJsonFile(filePrefix = TOKENS_FILE_NAME)
            Timber.tag("____").d("Metadata readed size = $file")
            file?.let { gson.fromJson(it.data, TokensMetadataInfo::class.java) }
        } catch (e: JsonSyntaxException) {
            Timber.tag("____").d("Metadata readed error")
            null
        }
    }

    private suspend fun updateLocalFile(tokensMetadata: TokensMetadataInfo) {
        val lastModified = tokensMetadata.timestamp
        Timber.tag(TAG).i("New tokens metadata received from: $lastModified, updating local file.")

        // Save tokens to the file
        externalStorageRepository.saveAsJsonFile(
            jsonObject = tokensMetadata,
            fileName = TOKENS_FILE_NAME
        )
    }

    private fun updateMemoryCache(tokensMetadata: TokensMetadataInfo?) {
        Timber.tag(TAG).i("Updating in-memory cache for tokens-metadata")

        if (tokensMetadata?.tokens == null) {
            Timber.tag(TAG).e("Local file not found!")
            return
        }

        userLocalRepository.setTokenData(tokensMetadata.tokens)
    }

    private fun handleError(throwable: Throwable) {
        Timber.tag(TAG).e(throwable, "Error loading metadata")
    }
}
