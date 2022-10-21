package org.p2p.wallet.home.ui.main

import androidx.annotation.RawRes
import androidx.annotation.StringRes
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class MainFragmentOnCreateAction : Parcelable {
    data class ShowSnackbar(@StringRes val messageResId: Int) : MainFragmentOnCreateAction()
    data class PlayAnimation(@RawRes val animationRes: Int) : MainFragmentOnCreateAction()
}
