package org.p2p.wallet.solend.ui.info

import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import org.p2p.wallet.utils.requireInt

private const val ICON_ARG = "sliderIcon"
private const val TEXT_ARG = "sliderText"

data class SolendInfoSliderFragmentArgs(
    @DrawableRes val iconRes: Int,
    @StringRes val slideText: Int,
) {
    fun toBundle(): Bundle = bundleOf(
        ICON_ARG to iconRes,
        TEXT_ARG to slideText
    )

    companion object {
        @Throws(IllegalArgumentException::class)
        fun fromBundle(bundle: Bundle?): SolendInfoSliderFragmentArgs {
            requireNotNull(bundle) { "Required arguments is missing!" }

            bundle.classLoader = SolendInfoSliderFragmentArgs::class.java.classLoader

            return SolendInfoSliderFragmentArgs(
                bundle.requireInt(ICON_ARG),
                bundle.requireInt(TEXT_ARG),
            )
        }
    }
}
