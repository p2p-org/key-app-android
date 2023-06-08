package org.p2p.wallet.auth.repository

import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.InputStream
import java.nio.file.Paths
import kotlin.test.assertNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.p2p.wallet.R
import org.p2p.wallet.auth.gateway.parser.CountryCodeXmlParser
import org.p2p.wallet.utils.UnconfinedTestDispatchers

@OptIn(ExperimentalCoroutinesApi::class)
class CountryCodeLocalRepositoryTest {

    private val dispatchers = UnconfinedTestDispatchers()

    private val currentWorkingDir = Paths.get("").toAbsolutePath().toString()
    private val assetsRoot = File(currentWorkingDir, "build/intermediates/assets/debug")
    private lateinit var repo: CountryCodeRepository

    @Before
    fun setUp() {
        repo = createRepository()
    }

    @Test
    fun `GIVEN xml with countries WHEN getCountryCodes THEN check countries list is not empty`() = runTest {
        val countries = repo.getCountryCodes()
        assertTrue(countries.isNotEmpty())
    }

    @Test
    fun `GIVEN xml with countries WHEN find US country by US phone code THEN country is not null`() = runTest {
        val country = repo.findCountryCodeByPhoneCode("1")
        assertNotNull(country)
    }

    @Test
    fun `GIVEN xml with countries WHEN find Turkey by alpha2 code with uppercase THEN country is not null`() = runTest {
        val country = repo.findCountryCodeByIsoAlpha2("TR")
        assertNotNull(country)
    }

    @Test
    fun `GIVEN xml with countries WHEN find Turkey by alpha2 code with lowercase THEN country is not null`() = runTest {
        val country = repo.findCountryCodeByIsoAlpha2("tr")
        assertNotNull(country)
    }

    @Test
    fun `GIVEN xml with countries WHEN find Turkey by alpha3 code with lowercase THEN country is not null`() = runTest {
        val country = repo.findCountryCodeByIsoAlpha3("tur")
        assertNotNull(country)
    }

    @Test
    fun `GIVEN xml with countries WHEN find Turkey by alpha3 code with uppercase THEN country is not null`() = runTest {
        val country = repo.findCountryCodeByIsoAlpha3("TUR")
        assertNotNull(country)
    }

    /**
     * This test is not actually about CountryCodeLocalRepository,
     * but we must be sure that every country has its mask
     */
    /* TODO
    @Test
    fun `GIVEN xml with countries WHEN all countries presenter THEN check every country has phone mask in CountryRepository`() =
        runTest {
            val countries = repo.getCountryCodes()

            val resources = mockk<Resources> {
                every { openRawResource(R.raw.phone_masks) } answers {
                    File(currentWorkingDir, "src/main/res/raw/phone_masks.txt").inputStream()
                }
            }

            val countryLocalRepo = CountryInMemoryRepository(
                countryCodeRepository = repo,
                resources = resources
            )

            countries.forEach { country ->
                val extractedCountry = Country(
                    name = country.countryName,
                    flagEmoji = "",
                    codeAlpha2 = country.nameCodeAlpha2,
                    codeAlpha3 = country.nameCodeAlpha3,
                )
                val phoneMask = countryLocalRepo.findPhoneMaskByCountry(extractedCountry)
                if(phoneMask == null) {
                    println("Country ${extractedCountry.name} (${extractedCountry.codeAlpha2}) doesn't have phone mask")
                }
//                assertTrue(, phoneMask != null)
            }
        }

     */

    private fun createRepository(): CountryCodeRepository {
        val assetManager: AssetManager = mockk {
            every { open(any()) } answers {
                val file = File(assetsRoot, arg<String>(0))
                if (!file.exists()) throw IllegalStateException("File not found: ${file.absolutePath}")
                file.inputStream()
            }
        }
        val context = mockk<Context> {
            every { assets } returns assetManager
        }
        val resources = mockk<Resources> {
            every { openRawResource(R.raw.ccp_english) } returns readCountriesXmlFile()
        }
        val phoneNumberUtil = PhoneNumberUtil.createInstance(context)

        val parser = CountryCodeXmlParser(resources, phoneNumberUtil)

        return CountryCodeInMemoryRepository(
            dispatchers = dispatchers,
            context = context,
            countryCodeHelper = parser
        )
    }

    private fun readCountriesXmlFile(): InputStream {
        val ccpEnglishFile = File(currentWorkingDir, "src/main/res/raw/ccp_english.xml")
        assertTrue(ccpEnglishFile.exists())

        return ccpEnglishFile.inputStream()
    }
}
