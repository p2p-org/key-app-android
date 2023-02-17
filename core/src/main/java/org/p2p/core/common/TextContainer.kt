package org.p2p.core.common

import androidx.annotation.StringRes
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

        operator fun invoke(text: String) =
            Raw(text)
    }

    abstract fun applyTo(textView: TextView)
    abstract fun getString(context: Context): String

    @Parcelize
    data class Res(@StringRes private val textRes: Int) : TextContainer(), Parcelable {
        override fun applyTo(textView: TextView) {
            textView.setText(textRes)
        }

        override fun getString(context: Context): String =
            context.getString(textRes)
    }

    data class ResParams(@StringRes val textRes: Int, val args: List<Any>) : TextContainer() {
        override fun applyTo(textView: TextView) {
            textView.text = textView.context.getString(textRes, *args.toTypedArray())
        }

        override fun getString(context: Context): String =
            context.getString(textRes, *args.toTypedArray())
    }

    @Parcelize
    data class Raw(
        private val text: CharSequence
    ) : TextContainer(), Parcelable {
        override fun applyTo(textView: TextView) {
            textView.text = text
        }

        override fun getString(context: Context): String = text.toString()
    }
}
