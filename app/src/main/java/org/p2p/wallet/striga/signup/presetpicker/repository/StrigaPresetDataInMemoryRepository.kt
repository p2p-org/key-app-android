package org.p2p.wallet.striga.signup.presetpicker.repository

import android.content.res.Resources
import com.google.gson.Gson
import com.google.gson.JsonArray
import timber.log.Timber
import java.io.InputStream
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.striga.signup.presetpicker.interactor.StrigaOccupation
import org.p2p.wallet.striga.signup.presetpicker.interactor.StrigaSourceOfFunds
import org.p2p.wallet.utils.unsafeLazy

private const val JSON_KEY_OCCUPATION_NAME = "occupation"
private const val JSON_KEY_OCCUPATION_EMOJI = "emoji"

class StrigaPresetDataInMemoryRepository(
    private val resources: Resources,
    private val gson: Gson
) : StrigaPresetDataLocalRepository {

    private val occupationValuesFile: InputStream
        get() = resources.openRawResource(R.raw.striga_occupation_values)

    private val sourceOfFundsFile: InputStream
        get() = resources.openRawResource(R.raw.striga_source_of_funds_values)

    /**
     * List of countries supported by Striga
     */
    private val supportedStrigaCountries by unsafeLazy {
        setOf(
            "at", "be", "bg", "hr",
            "cy", "cz", "dk", "ee",
            "fi", "fr", "gr", "es",
            "nl", "is", "li", "lt",
            "lu", "lv", "mt", "de",
            "no", "pl", "pt", "ro",
            "sk", "si", "se", "hu",
            "it", "ch", "gb"
        )
    }

    private var cachedOccupation: List<StrigaOccupation> = emptyList()
    private var cachedSourceOfFunds: List<StrigaSourceOfFunds> = emptyList()

    override fun getOccupationValuesList(): List<StrigaOccupation> {
        return cachedOccupation.ifEmpty(::parseOccupationFile)
    }

    override fun getSourceOfFundsList(): List<StrigaSourceOfFunds> {
        return cachedSourceOfFunds.ifEmpty(::parseSourceOfFundsFile)
    }

    override fun checkIsCountrySupported(country: CountryCode): Boolean {
        return country.nameCodeAlpha2.lowercase() in supportedStrigaCountries
    }

    private fun parseOccupationFile(): List<StrigaOccupation> {
        return runCatching { gson.fromJson(occupationValuesFile.reader(), JsonArray::class.java) }
            .onFailure { Timber.i(it) }
            .getOrDefault(JsonArray())
            .map {
                it.asJsonObject.run {
                    StrigaOccupation(
                        occupationName = get(JSON_KEY_OCCUPATION_NAME).asString,
                        emoji = get(JSON_KEY_OCCUPATION_EMOJI).asString
                    )
                }
            }
            .also { cachedOccupation = it }
    }

    private fun parseSourceOfFundsFile(): List<StrigaSourceOfFunds> {
        return runCatching { gson.fromJson(sourceOfFundsFile.reader(), JsonArray::class.java) }
            .onFailure { Timber.i(it) }
            .getOrDefault(JsonArray())
            .map { StrigaSourceOfFunds(it.asString) }
            .also { cachedSourceOfFunds = it }
    }
}
