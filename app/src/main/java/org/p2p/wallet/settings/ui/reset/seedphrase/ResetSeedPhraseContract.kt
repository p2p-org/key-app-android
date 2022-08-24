package org.p2p.wallet.settings.ui.reset.seedphrase

import androidx.annotation.StringRes
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.uikit.organisms.seedphrase.SeedPhraseKey

interface ResetSeedPhraseContract {

    interface View : MvpView {
        fun showSuccess(secretKeys: List<SeedPhraseKey>)
        fun showError(@StringRes messageRes: Int)
        fun setButtonEnabled(isEnabled: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun setNewKeys(keys: List<SeedPhraseKey>)
        fun verifySeedPhrase()
    }
}
