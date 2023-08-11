package org.p2p.wallet.countrycode

import android.content.res.Resources
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.reflect.Type
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.wallet.R
import org.p2p.wallet.common.storage.ExternalStorageRepository
import org.p2p.wallet.countrycode.mapper.ExternalCountryCodeMapper
import org.p2p.wallet.countrycode.model.ExternalCountryCode
import org.p2p.wallet.countrycode.model.ExternalCountryCodeEntity
import org.p2p.wallet.home.events.AppLoader

class ExternalCountryCodeLoader(
    resources: Resources,
    private val dispatchers: CoroutineDispatchers,
    private val okHttpClient: OkHttpClient,
    private val externalStorageRepository: ExternalStorageRepository,
    private val gson: Gson,
    private val mapper: ExternalCountryCodeMapper,
) : AppLoader() {

    companion object {
        const val TAG = "ExternalCountryCodeLoader"
        private const val COUNTRY_LIST_FILE_NAME = "country_list.json"
        private val LIST_TYPE: Type = object : TypeToken<List<ExternalCountryCodeEntity>>() {}.type
    }

    private val countryCodeListUrl: String = resources.getString(R.string.countryCodeListBaseUrl)
    private val mutex = Mutex()

    override suspend fun onLoad() {
        withContext(dispatchers.io) { downloadFileIfNeeded() }
    }

    override suspend fun onRefresh() {
        externalStorageRepository.deleteFile(COUNTRY_LIST_FILE_NAME)
        onLoad()
    }

    @Throws(ExternalCountryCodeError::class, JsonParseException::class)
    suspend fun loadAndSaveFile(): List<ExternalCountryCode> = withContext(dispatchers.io) {
        downloadFileIfNeeded()

        val body = externalStorageRepository.readJsonFile(COUNTRY_LIST_FILE_NAME)?.data
            ?: throw ExternalCountryCodeError.LocalFileReadError("Can't read existing file $COUNTRY_LIST_FILE_NAME")

        try {
            gson.fromJson<List<ExternalCountryCodeEntity>>(body, LIST_TYPE)
                .map(mapper::fromEntity)
        } catch (e: JsonParseException) {
            throw ExternalCountryCodeError.ParseError(e)
        } catch (e: Throwable) {
            throw ExternalCountryCodeError.UnknownError(e)
        }
    }

    private suspend fun downloadFileIfNeeded() {
        // double-check to prevent parallel file downloading
        if (!externalStorageRepository.isFileExists(COUNTRY_LIST_FILE_NAME)) {
            mutex.withLock {
                if (!externalStorageRepository.isFileExists(COUNTRY_LIST_FILE_NAME)) {
                    downloadFile()
                }
            }
        }
    }

    private suspend fun downloadFile() {
        return try {
            val request = Request.Builder().get().url(countryCodeListUrl).build()
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                throw ExternalCountryCodeError.HttpError("Unable to download country list", response)
            }

            val body = response.body ?: throw NullPointerException("Response body is null")
            if (body.contentLength() == 0L) {
                throw ExternalCountryCodeError.EmptyResponse()
            }

            externalStorageRepository.saveRawFile(COUNTRY_LIST_FILE_NAME, body.byteStream())
        } catch (e: ExternalCountryCodeError) {
            throw e
        } catch (e: Throwable) {
            throw ExternalCountryCodeError.UnknownError(e)
        }
    }
}
