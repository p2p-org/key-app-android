package org.p2p.wallet.auth.ui.generalerror

import android.os.Parcelable
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.PhoneNumber

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
        val userPhoneNumber: PhoneNumber
    ) : GeneralErrorScreenError()

    object DeviceShareNotFound : GeneralErrorScreenError()

    data class NoTokenFound(val tokenId: String) : GeneralErrorScreenError()
}
