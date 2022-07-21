package org.p2p.wallet.auth.ui.onboarding

import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.p2p.wallet.utils.requireInt

private const val ICON_ARG = "sliderIcon"
private const val TITLE_ARG = "sliderTitle"
private const val TEXT_ARG = "sliderText"

data class SliderFragmentArgs(
    @DrawableRes val iconRes: Int,
    @StringRes val slideTitle: Int,
    @StringRes val slideText: Int,
) {
    fun toBundle(): Bundle = Bundle().apply {
        putInt(ICON_ARG, iconRes)
        putInt(TITLE_ARG, slideTitle)
        putInt(TEXT_ARG, slideText)
    }

    companion object {
        @Throws(IllegalArgumentException::class)
        fun fromBundle(bundle: Bundle?): SliderFragmentArgs {
            requireNotNull(bundle) { "Required arguments is missing!" }

            bundle.classLoader = SliderFragmentArgs::class.java.classLoader

            return SliderFragmentArgs(
                bundle.requireInt(ICON_ARG),
                bundle.requireInt(TITLE_ARG),
                bundle.requireInt(TEXT_ARG),
            )
        }
    }
}
