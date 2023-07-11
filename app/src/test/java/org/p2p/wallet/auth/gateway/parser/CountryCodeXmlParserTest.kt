package org.p2p.wallet.auth.gateway.parser

import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertTrue
import org.junit.ClassRule
import org.junit.Test
import java.io.File
import java.io.InputStream
import java.nio.file.Paths
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.p2p.wallet.R
import org.p2p.wallet.utils.TimberUnitTestInstance

@OptIn(ExperimentalCoroutinesApi::class)
class CountryCodeXmlParserTest {

    companion object {
        @ClassRule
        @JvmField
        val timber = TimberUnitTestInstance(
            isEnabled = false,
            defaultTag = "CountryCodeXmlParserTest"
        )
    }

    private val currentWorkingDir = Paths.get("").toAbsolutePath().toString()
    private val assetsRoot = File(currentWorkingDir, "build/intermediates/assets/debug")

    @Test
    fun `GIVEN real xml WHEN parse THEN return list of CountryCode`() = runTest {
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

        var error: Throwable? = null
        try {
            val parser = CountryCodeXmlParser(resources, phoneNumberUtil)
            val countryList = parser.parserCountryCodesFromXmlFile()
            assertTrue(countryList.isNotEmpty())
            countryList.forEach {
                assertTrue(it.countryName.isNotEmpty())
                assertTrue(it.nameCodeAlpha2.isNotEmpty())
                assertTrue(it.nameCodeAlpha3.isNotEmpty())
                assertTrue(it.flagEmoji.isNotEmpty())
            }
        } catch (e: Throwable) {
            error = e
        }

        error?.let { throw it }
    }

    private fun readCountriesXmlFile(): InputStream {
        val ccpEnglishFile = File(currentWorkingDir, "src/main/res/raw/ccp_english.xml")
        assertTrue(ccpEnglishFile.exists())

        return ccpEnglishFile.inputStream()
    }
}
