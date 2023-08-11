package org.p2p.wallet.countrycode

import android.content.res.Resources
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import com.google.gson.Gson
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody.Companion.toResponseBody
import org.intellij.lang.annotations.Language
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import java.io.InputStream
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.wallet.common.storage.ExternalFile
import org.p2p.wallet.common.storage.ExternalStorageRepository
import org.p2p.wallet.countrycode.mapper.ExternalCountryCodeMapper
import org.p2p.wallet.countrycode.model.ExternalCountryCode
import org.p2p.wallet.utils.StandardTestCoroutineDispatchers
import org.p2p.wallet.utils.assertThat
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.mocks.answersResponseBody

@OptIn(ExperimentalCoroutinesApi::class)
class ExternalCountryCodeLoaderTest {

    @Language("JSON")
    private val countryListResponseBody = """
    [
        {
            "name": "Andorra",
            "alpha2": "AD",
            "alpha3": "AND",
            "flag_emoji": "&#127462;&#127465;",
            "is_striga_allowed": false,
            "is_moonpay_allowed": true
        },
        {
            "name": "United Arab Emirates",
            "alpha2": "AE",
            "alpha3": "ARE",
            "flag_emoji": null,
            "is_striga_allowed": true,
            "is_moonpay_allowed": false
        }
    ]
    """.trimIndent()

    private val dispatchers: CoroutineDispatchers = StandardTestCoroutineDispatchers()

    @MockK(relaxed = true)
    private lateinit var resources: Resources

    @MockK(relaxed = true)
    private lateinit var okHttpClient: OkHttpClient

    @MockK
    private lateinit var externalStorageRepository: ExternalStorageRepository

    private val gson = Gson()
    private val mapper = ExternalCountryCodeMapper()

    private var savedFile: String? = null

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { resources.getString(any()) } returns "https://google.com"

        every { okHttpClient.newCall(any()) } answersResponseBody {
            countryListResponseBody.toResponseBody("application/json".toMediaType())
        }

        every { externalStorageRepository.deleteFile(any()) } answers {
            savedFile = null
        }
        every { externalStorageRepository.isFileExists(any()) } answers { savedFile != null }
        coEvery { externalStorageRepository.readJsonFile(any()) } answers {
            savedFile?.let(::ExternalFile)
        }
        coEvery { externalStorageRepository.saveRawFile(any(), any<String>()) } answers {
            savedFile = secondArg()
        }
        coEvery { externalStorageRepository.saveRawFile(any(), any<InputStream>()) } answers {
            savedFile = secondArg<InputStream>().reader().readText()
        }
    }

    @BeforeEach
    fun setUpEach() {
        savedFile = null
    }

    private fun createLoader(): ExternalCountryCodeLoader {
        return ExternalCountryCodeLoader(
            resources = resources,
            dispatchers = dispatchers,
            okHttpClient = okHttpClient,
            externalStorageRepository = externalStorageRepository,
            gson = gson,
            mapper = mapper
        )
    }

    @Test
    fun `GIVEN valid country list WHEN loadAndSaveFile THEN check result is correct list`() = runTest {
        val loader = createLoader()
        val result = loader.loadAndSaveFile()

        result.assertThat().hasSize(2)
        result.first().assertThat()
            .isEqualTo(
                ExternalCountryCode(
                    countryName = "Andorra",
                    nameCodeAlpha2 = "AD",
                    nameCodeAlpha3 = "AND",
                    flagEmoji = "&#127462;&#127465;",
                    isStrigaAllowed = false,
                    isMoonpayAllowed = true
                )
            )
        result.last().assertThat()
            .isEqualTo(
                ExternalCountryCode(
                    countryName = "United Arab Emirates",
                    nameCodeAlpha2 = "AE",
                    nameCodeAlpha3 = "ARE",
                    flagEmoji = null,
                    isStrigaAllowed = true,
                    isMoonpayAllowed = false
                )
            )
    }

    @Test
    fun `GIVEN http error WHEN called loadAndSaveFile THEN check exceptions is thrown`() = runTest {
        every { okHttpClient.newCall(any()) }.answersResponseBody(500, "FATAL ERROR") {
            countryListResponseBody.toResponseBody("application/json".toMediaType())
        }

        val loader = createLoader()
        assertThrows<ExternalCountryCodeError.HttpError> {
            loader.loadAndSaveFile()
        }
    }

    @Test
    fun `GIVEN http success but empty response WHEN called loadAndSaveFile THEN check EmptyResponse exception`() =
        runTest {
            every { okHttpClient.newCall(any()) }.answersResponseBody(200, "OK") {
                emptyString().toResponseBody("application/json".toMediaType())
            }

            val loader = createLoader()
            assertThrows<ExternalCountryCodeError.EmptyResponse> {
                loader.loadAndSaveFile()
            }
        }

    @Test
    fun `GIVEN http success but malformed response WHEN called loadAndSaveFile THEN check ParseError exception`() =
        runTest {
            every { okHttpClient.newCall(any()) }.answersResponseBody(200, "OK") {
                "[{\"name\": \"Andorra\",".toResponseBody("application/json".toMediaType())
            }

            val loader = createLoader()
            assertThrows<ExternalCountryCodeError.ParseError> {
                loader.loadAndSaveFile()
            }
        }

    @Test
    fun `GIVEN http success but wrong syntax response WHEN called loadAndSaveFile THEN check ParseError exception`() =
        runTest {
            @Language("JSON")
            val syntaxErrorJson = """
                [
                    {
                        "name": "Andorra",
                        "alpha2": "AD",
                        "alpha3": "AND",,
                        "flag_emoji": "&#127462;&#127465;",
                        "is_striga_allowed": false,
                        "is_moonpay_allowed": true
                    }
                ]
            """.trimIndent()
            every { okHttpClient.newCall(any()) }.answersResponseBody(200, "OK") {
                syntaxErrorJson.toResponseBody("application/json".toMediaType())
            }

            val loader = createLoader()
            assertThrows<ExternalCountryCodeError.ParseError> {
                loader.loadAndSaveFile()
            }
        }
}
