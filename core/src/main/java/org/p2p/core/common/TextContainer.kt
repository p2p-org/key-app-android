package org.p2p.core.common

import android.content.Context
import android.os.Parcelable
import android.widget.TextView
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize

sealed class TextContainer : Parcelable {
    companion object {
        operator fun invoke(@StringRes textRes: Int) =
            Res(textRes)

        operator fun invoke(text: String) =
            Raw(text)
    }

    abstract fun applyTo(textView: TextView)
    abstract fun getString(context: Context): String

    @Parcelize
    class Res(@StringRes private val textRes: Int) : TextContainer() {
        override fun applyTo(textView: TextView) {
            textView.setText(textRes)
        }

        override fun getString(context: Context): String =
            context.getString(textRes)
    }

    @Parcelize
    class Raw(
        private val text: CharSequence
    ) : TextContainer() {
        override fun applyTo(textView: TextView) {
            textView.text = text
        }

        override fun getString(context: Context): String = text.toString()
    }
}
