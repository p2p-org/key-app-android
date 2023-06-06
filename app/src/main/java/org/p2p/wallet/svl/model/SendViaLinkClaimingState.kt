package org.p2p.wallet.svl.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.p2p.core.token.Token
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R

sealed interface SendViaLinkClaimingState {
    object InitialLoading : SendViaLinkClaimingState

    data class ReadyToClaim(
        val temporaryAccount: TemporaryAccount,
        val token: Token.Active
    ) : SendViaLinkClaimingState

    object ClaimingInProcess : SendViaLinkClaimingState

    data class ClaimSuccess(val successMessage: TextViewCellModel) : SendViaLinkClaimingState

    data class ClaimFailed(@StringRes val errorMessageRes: Int) : SendViaLinkClaimingState

    data class ParsingFailed(
        @StringRes val titleRes: Int,
        @StringRes val subTitleRes: Int?,
        @DrawableRes val iconRes: Int
    ) : SendViaLinkClaimingState {

        companion object {
            fun buildUnknownError(): ParsingFailed = ParsingFailed(
                titleRes = R.string.send_via_link_error_failed_parsing_title,
                subTitleRes = null,
                iconRes = R.drawable.ic_not_found
            )

            fun buildInternetError(): ParsingFailed = ParsingFailed(
                titleRes = R.string.error_no_internet_message_no_emoji,
                subTitleRes = null,
                iconRes = R.drawable.ic_cat
            )
        }
    }
}
