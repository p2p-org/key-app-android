package org.p2p.wallet.striga.signup.repository.model

import androidx.annotation.StringRes
import android.content.res.Resources
import org.p2p.wallet.R

enum class StrigaSignupDataType(@StringRes val tag: Int) {
    EMAIL(R.string.tag_striga_email),
    // used internally
    PHONE_CODE(R.string.tag_striga_phone_code),
    PHONE_NUMBER(R.string.tag_striga_phone_number),
    FIRST_NAME(R.string.tag_striga_first_name),
    LAST_NAME(R.string.tag_striga_last_name),
    DATE_OF_BIRTH(R.string.tag_striga_date_of_birth),
    /**
     * Must be ISO 3166-1 alpha-3 code (3 symbols)
     */
    COUNTRY_OF_BIRTH(R.string.tag_striga_country_of_birth),
    OCCUPATION(R.string.tag_striga_occupation),
    SOURCE_OF_FUNDS(R.string.tag_striga_source_of_funds),
    /**
     * Must be ISO 3166-1 alpha-2 code (2 symbols)
     */
    COUNTRY(R.string.tag_striga_country),
    CITY(R.string.tag_striga_city),
    CITY_ADDRESS_LINE(R.string.tag_striga_city_address_line),
    CITY_POSTAL_CODE(R.string.tag_striga_city_postal_code),
    CITY_STATE(R.string.tag_striga_city_state);

    companion object {
        fun fromTag(
            tagValue: String,
            resources: Resources
        ): StrigaSignupDataType? = values().firstOrNull { resources.getString(it.tag) == tagValue }
    }
}
