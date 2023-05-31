package org.p2p.wallet.auth.repository

import android.content.res.Resources
import timber.log.Timber
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.CountryCode

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

    override suspend fun findCountryByNameCode(countyCode: String): Country? {
        return countryCodeRepository.findCountryCodeByPhoneCode(countyCode)?.let {
            return it.extractCountry()
        }
    }

    override suspend fun findPhoneMaskByCountry(country: Country): String? {
        try {
            val needleCountry = country.code.uppercase()
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
                    return null
                }

                return lines[soughtLine].substring(3)
            }
        } catch (e: IOException) {
            Timber.e(e, "Unable to read phone masks")
        }
        return null
    }

    private fun CountryCode.extractCountry(): Country = Country(countryName, flagEmoji, nameCode)

    private suspend fun defaultCountry(): Country = getAllCountries().first()
}
