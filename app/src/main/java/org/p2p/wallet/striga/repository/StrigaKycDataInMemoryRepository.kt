package org.p2p.wallet.striga.repository

import android.content.res.Resources
import com.google.gson.Gson
import com.google.gson.JsonArray
import timber.log.Timber
import java.io.InputStream
import org.p2p.wallet.R
import org.p2p.wallet.striga.repository.model.StrigaOccupation
import org.p2p.wallet.striga.repository.model.StrigaSourceOfFunds

private const val JSON_KEY_OCCUPATION_NAME = "occupation"
private const val JSON_KEY_OCCUPATION_EMOJI = "emoji"

class StrigaKycDataInMemoryRepository(
    private val resources: Resources,
    private val gson: Gson
) : StrigaKycDataLocalRepository {

    private val occupationValuesFile: InputStream
        get() = resources.openRawResource(R.raw.striga_occupation_values)

    private val sourceOfFundsFile: InputStream
        get() = resources.openRawResource(R.raw.striga_source_of_funds_values)

    private var cachedOccupation: List<StrigaOccupation> = emptyList()
    private var cachedSourceOfFunds: List<StrigaSourceOfFunds> = emptyList()

    override fun getOccupationValuesList(): List<StrigaOccupation> {
        return cachedOccupation.ifEmpty(::parseOccupationValuesFile)
    }

    override fun getSourceOfFundsList(): List<StrigaSourceOfFunds> {
        return cachedSourceOfFunds.ifEmpty(::parseSourceOfFundsValuesFile)
    }

    private fun parseOccupationValuesFile(): List<StrigaOccupation> {
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

    private fun parseSourceOfFundsValuesFile(): List<StrigaSourceOfFunds> {
        return runCatching { gson.fromJson(sourceOfFundsFile.reader(), JsonArray::class.java) }
            .onFailure { Timber.i(it) }
            .getOrDefault(JsonArray())
            .map { StrigaSourceOfFunds(it.asString) }
            .also { cachedSourceOfFunds = it }
    }
}
