package org.p2p.wallet.moonpay.ui.transaction

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import org.p2p.wallet.R

class SellTransactionDetailsViewState(
    val titleBlock: TitleBlock,
    val messageBlock: BodyBlock,
    val receiverBlock: ReceiverBlock?,
    val buttonsBlock: ButtonsBlock
) {
    class TitleBlock(
        val title: String,
        val updatedAt: String,
        val boldAmount: String,
        val labelAmount: String?,
    )

    class BodyBlock private constructor(
        val bodyText: CharSequence, // can be spanned
        @ColorRes val bodyTextColor: Int = R.color.text_night,
        @ColorRes val bodyBackgroundColor: Int,
        @DrawableRes val bodyIconRes: Int,
        @ColorRes val bodyIconTint: Int,
    ) {
        companion object {
            fun rain(bodyText: CharSequence): BodyBlock = BodyBlock(
                bodyText = bodyText,
                bodyTextColor = R.color.text_night,
                bodyBackgroundColor = R.color.bg_rain,
                bodyIconRes = R.drawable.ic_alert_rounded,
                bodyIconTint = R.color.icons_sun
            )

            fun silver(bodyText: CharSequence): BodyBlock = BodyBlock(
                bodyText = bodyText,
                bodyBackgroundColor = R.color.light_silver,
                bodyIconRes = R.drawable.ic_info_rounded,
                bodyIconTint = R.color.icons_silver
            )

            fun rose(bodyText: CharSequence): BodyBlock = BodyBlock(
                bodyText = bodyText,
                bodyTextColor = R.color.text_rose,
                bodyBackgroundColor = R.color.rose_20,
                bodyIconRes = R.drawable.ic_alert_rounded,
                bodyIconTint = R.color.icons_rose
            )
        }
    }

    class ReceiverBlock(
        val receiverTitle: String,
        val receiverValue: String,
        val isCopyEnabled: Boolean,
        val copyValueProvider: (() -> String)? = null
    )

    class ButtonsBlock(
        val mainButtonTitle: String? = null,
        val mainButtonAction: (() -> Unit)? = null,
        val additionalButtonTitle: String? = null,
        val additionalButtonAction: (() -> Unit)? = null
    )
}
