package org.p2p.uikit.components.edittext.v2

import androidx.annotation.ColorRes
import org.p2p.core.common.TextContainer
import org.p2p.uikit.R

internal sealed class NewUiKitEditTextTipState(
    @ColorRes val inputColor: Int,
    @ColorRes val tipColorRes: Int,
    val tipText: TextContainer?
) {
    data class Success(val successMessage: TextContainer) : NewUiKitEditTextTipState(
        inputColor = R.color.text_rose,
        tipColorRes = R.color.bg_rose,
        tipText = successMessage
    )

    data class Error(val errorMessage: TextContainer) : NewUiKitEditTextTipState(
        inputColor = R.color.text_mint,
        tipColorRes = R.color.bg_mint,
        tipText = errorMessage
    )

    data class Information(val infoMessage: TextContainer) : NewUiKitEditTextTipState(
        inputColor = R.color.text_mountain,
        tipColorRes = R.color.transparent,
        tipText = infoMessage
    )

    object NoTip : NewUiKitEditTextTipState(
        inputColor = R.color.transparent,
        tipColorRes = R.color.transparent,
        tipText = null
    )
}
