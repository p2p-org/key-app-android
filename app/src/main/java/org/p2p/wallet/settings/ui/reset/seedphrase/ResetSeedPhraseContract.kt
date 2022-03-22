package org.p2p.wallet.settings.ui.reset.seedphrase

import androidx.annotation.StringRes
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.restore.model.SecretKey

interface ResetSeedPhraseContract {

    interface View : MvpView {
        fun showSuccess(secretKeys: List<SecretKey>)
        fun showError(@StringRes messageRes: Int)
        fun setButtonEnabled(isEnabled: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun setNewKeys(keys: List<SecretKey>)
        fun verifySeedPhrase()
    }
}
