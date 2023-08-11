package org.p2p.wallet.countrycode.parser

import android.content.res.Resources
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.reflect.Type
import kotlinx.coroutines.withContext
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.utils.bodyAsString
import org.p2p.wallet.R
import org.p2p.wallet.common.storage.ExternalStorageRepository
import org.p2p.wallet.countrycode.ExternalCountryCodeLoadError
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

    override suspend fun onLoad() {
        if (!externalStorageRepository.isFileExists(COUNTRY_LIST_FILE_NAME)) {
            withContext(dispatchers.io) { downloadFile() }
        }
    }

    override suspend fun onRefresh() {
        externalStorageRepository.deleteFile(COUNTRY_LIST_FILE_NAME)
        onLoad()
    }

    override suspend fun isEnabled(): Boolean = true

    @Throws(ExternalCountryCodeLoadError::class, JsonParseException::class, JsonSyntaxException::class)
    suspend fun loadAndSaveFile(): List<ExternalCountryCode> = withContext(dispatchers.io) {
        val body: String = if (!externalStorageRepository.isFileExists(COUNTRY_LIST_FILE_NAME)) {
            downloadFile()
        } else {
            externalStorageRepository.readJsonFile(COUNTRY_LIST_FILE_NAME)?.data
                ?: error("Can't read existing file $COUNTRY_LIST_FILE_NAME")
        }

        gson.fromJson<List<ExternalCountryCodeEntity>>(body, LIST_TYPE)
            .map(mapper::fromEntity)
    }

    private suspend fun downloadFile(): String {
        return executeRequest().also { body ->
            externalStorageRepository.saveRawFile(COUNTRY_LIST_FILE_NAME, body)
        }
    }

    private fun executeRequest(): String {
        return try {
            val request = Request.Builder().get().url(countryCodeListUrl).build()
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                error(
                    buildString {
                        append("Error while loading country code list: ")
                        append("[${response.code}] ${response.message} ${response.bodyAsString()}")
                    }
                )
            }
            response.bodyAsString()
        } catch (e: Throwable) {
            throw ExternalCountryCodeLoadError(e)
        }
    }
}
