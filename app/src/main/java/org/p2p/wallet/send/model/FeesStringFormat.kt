package org.p2p.wallet.send.model

import androidx.annotation.StringRes
import android.content.res.Resources

class FeesStringFormat(
    @StringRes val textRes: Int,
    vararg val arguments: String
) {

    fun format(resources: Resources): String {
        return if (arguments.isEmpty()) {
            resources.getString(textRes)
        } else {
            resources.getString(textRes, *arguments)
        }
    }
}
