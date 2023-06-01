package org.p2p.wallet.auth.repository

import android.content.res.Resources
import timber.log.Timber
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.auth.model.PhoneMask

class CountryInMemoryRepository(
    private val countryCodeRepository: CountryCodeLocalRepository,
    private val resources: Resources,
) : CountryRepository {
    override suspend fun getAllCountries(): List<Country> {
        return countryCodeRepository.getCountryCodes()
            .map { it.extractCountry() }
    }

    override suspend fun detectCountryOrDefault(): Country {
        val detectedCountryCode = countryCodeRepository.detectCountryCodeBySimCard()
            ?: countryCodeRepository.detectCountryCodeByNetwork()
            ?: countryCodeRepository.detectCountryCodeByLocale()

        return detectedCountryCode?.extractCountry() ?: defaultCountry()
    }

    override suspend fun findCountryByIsoAlpha3(countyCode: String): Country? {
        return countryCodeRepository.findCountryCodeByIsoAlpha3(countyCode)?.let {
            return it.extractCountry()
        }
    }

    override suspend fun findCountryByIsoAlpha2(countyCode: String): Country? {
        return countryCodeRepository.findCountryCodeByIsoAlpha2(countyCode)?.let {
            return it.extractCountry()
        }
    }

    override suspend fun findPhoneMaskByCountry(country: Country): PhoneMask? {
        return try {
            val needleCountry = country.codeAlpha2.uppercase()
            // phone_masks.txt is a sorted list
            val inputStream = resources.openRawResource(R.raw.phone_masks)
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                val lines = reader.lineSequence().toList()
                val soughtLine = lines.binarySearch { line ->
                    val lineCountryCode = line.substring(0, 2)
                    when {
                        lineCountryCode == needleCountry -> 0
                        lineCountryCode < needleCountry -> -1
                        else -> 1
                    }
                }

                if (soughtLine < 0) {
                    null
                } else {
                    parsePhoneMask(lines[soughtLine])
                }
            }
        } catch (e: IOException) {
            Timber.e(e, "Unable to read phone masks")
            null
        }
    }

    private fun CountryCode.extractCountry(): Country = Country(
        name = countryName,
        flagEmoji = flagEmoji,
        codeAlpha2 = nameCodeAlpha2,
        codeAlpha3 = nameCodeAlpha3,
    )

    private suspend fun defaultCountry(): Country = getAllCountries().first()

    private fun parsePhoneMask(mask: String): PhoneMask {
        // format: AR:54 ### ### ####
        val countryCode = mask.substringBefore(":")
        val phoneMask = mask.substringAfter(":").trim()
        val phoneCode = mask.substringBetween(":", " ")

        return PhoneMask(
            countryCodeAlpha2 = countryCode,
            phoneCode = phoneCode,
            mask = phoneMask,
        )
    }

    private fun String.substringBetween(start: String, end: String): String {
        val startIndex = indexOf(start) + start.length
        val endIndex = indexOf(end)
        return substring(startIndex, endIndex)
    }
}
