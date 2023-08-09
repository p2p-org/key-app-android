package org.p2p.wallet.striga.signup.presetpicker

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import android.content.res.Resources

data class SelectItemsStrings(
    @StringRes val toolbarTitleRes: Int,
    @PluralsRes val plural: Int
) {
    fun getSingularString(resources: Resources): String {
        return resources.getQuantityString(plural, 1)
    }

    fun getPluralString(resources: Resources, quantity: Int = 3): String {
        return resources.getQuantityString(plural, quantity)
    }
}
