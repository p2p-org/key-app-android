package org.p2p.wallet.striga.model

import androidx.annotation.StringRes
import android.content.res.Resources
import org.p2p.wallet.R

enum class StrigaSignupDataType(@StringRes val tag: Int) {
    FIRST_NAME(R.string.tag_striga_first_name);

    companion object {
        val cachedValues: Array<StrigaSignupDataType> = values()

        fun fromTag(
            tagValue: String,
            resources: Resources
        ): StrigaSignupDataType? = cachedValues.firstOrNull { resources.getString(it.tag) == tagValue }
    }
}
