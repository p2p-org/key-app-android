package org.p2p.wallet.auth.repository

import android.content.res.Resources
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

    override suspend fun findPhoneMaskByCountry(country: Country): String? {
        try {
            val needleCountry = country.code.uppercase()
            // phone_masks.txt is a sorted list
            val inputStream = resources.openRawResource(R.raw.phone_masks)
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                val lines = reader.lineSequence().toList()
                var low = 0
                var high = lines.count() - 1

                while (low <= high) {
                    val mid = (low + high) / 2
                    val line = lines[mid]
                    val lineCountryCode = line.substring(0, 2)

                    if (lineCountryCode == needleCountry) {
                        return line.substring(3)
                    } else if (lineCountryCode < needleCountry) {
                        low = mid + 1
                    } else {
                        high = mid - 1
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun CountryCode.extractCountry(): Country = Country(countryName, flagEmoji, nameCode)

    private suspend fun defaultCountry(): Country = getAllCountries().first()
}
