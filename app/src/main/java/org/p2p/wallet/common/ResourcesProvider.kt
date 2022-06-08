package org.p2p.wallet.common

import android.content.Context
import android.content.res.Resources
import androidx.annotation.ColorRes
import androidx.annotation.StringRes

class ResourcesProvider(private val context: Context) {

    val resources: Resources = context.resources

    fun getColor(@ColorRes color: Int) = context.getColor(color)

    fun getString(@StringRes stringRes: Int) = context.getString(stringRes)

    fun getString(@StringRes stringRes: Int, vararg formatArgs: Any) = if (formatArgs.isNotEmpty()) {
        context.getString(stringRes, *formatArgs)
    } else {
        getString(stringRes)
    }
}
