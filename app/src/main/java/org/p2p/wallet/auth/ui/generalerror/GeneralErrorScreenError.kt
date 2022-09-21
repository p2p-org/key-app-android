package org.p2p.wallet.auth.ui.generalerror

import android.os.Parcelable
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize
import org.p2p.wallet.R

@Parcelize
sealed class GeneralErrorScreenError(
    @StringRes val titleResId: Int? = null,
    @StringRes val messageResId: Int? = null
) : Parcelable {
    @Parcelize
    class CriticalError(val errorCode: Int) : GeneralErrorScreenError()

    object PhoneNumberDoesNotMatchError : GeneralErrorScreenError(
        titleResId = R.string.error_wallet_not_found_title,
        messageResId = R.string.error_wallet_not_found_message
    )

    data class AccountNotFound(
        val isDeviceShareExists: Boolean,
        val title: String,
        val message: String
    ) : GeneralErrorScreenError()
}
