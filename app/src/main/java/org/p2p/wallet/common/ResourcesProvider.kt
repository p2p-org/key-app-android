package org.p2p.wallet.common

import androidx.annotation.ColorRes
import androidx.annotation.IntegerRes
import androidx.annotation.StringRes
import android.content.Context
import android.content.res.Resources

@Deprecated(
    message = "This class is redundant",
    replaceWith = ReplaceWith("private val resources: Resources")
)
class ResourcesProvider(private val context: Context) {

    val resources: Resources = context.resources

    fun getColor(@ColorRes color: Int) = context.getColor(color)

    fun getString(@StringRes stringRes: Int) = context.getString(stringRes)

    fun getString(@StringRes stringRes: Int, vararg formatArgs: String) = if (formatArgs.isNotEmpty()) {
        context.getString(stringRes, *formatArgs)
    } else {
        getString(stringRes)
    }

    fun getString(@StringRes stringRes: Int, vararg formatArgs: Int) = if (formatArgs.isNotEmpty()) {
        context.getString(stringRes, formatArgs)
    } else {
        getString(stringRes)
    }

    fun getInteger(@IntegerRes integerRes: Int): Int = context.resources.getInteger(integerRes)
}
