package org.p2p.core.common

import androidx.annotation.StringRes
import androidx.core.view.isVisible
import android.content.Context
import android.os.Parcelable
import android.widget.TextView
import kotlinx.parcelize.Parcelize

sealed class TextContainer {
    companion object {
        operator fun invoke(@StringRes textRes: Int) =
            Res(textRes)

        operator fun invoke(@StringRes textRes: Int, vararg args: Any) =
            ResParams(textRes, args.toList())

        operator fun invoke(text: CharSequence) =
            Raw(text)
    }

    abstract fun getString(context: Context): String

    @Parcelize
    data class Res(@StringRes val textRes: Int) : TextContainer(), Parcelable {
        override fun getString(context: Context): String =
            context.getString(textRes)
    }

    data class ResParams(@StringRes val textRes: Int, val args: List<Any>) : TextContainer() {
        override fun getString(context: Context): String =
            context.getString(textRes, *args.toTypedArray())
    }

    @Parcelize
    data class Raw(
        val text: CharSequence
    ) : TextContainer(), Parcelable {
        override fun getString(context: Context): String = text.toString()
    }
}

fun TextView.bind(textContainer: TextContainer) {
    when (textContainer) {
        is TextContainer.Raw ->
            text = textContainer.text
        is TextContainer.Res ->
            setText(textContainer.textRes)
        is TextContainer.ResParams ->
            text = context.getString(textContainer.textRes, *textContainer.args.toTypedArray())
    }
}

fun TextView.bindOrGone(textContainer: TextContainer?) {
    isVisible = textContainer != null
    textContainer?.also(::bind)
}
