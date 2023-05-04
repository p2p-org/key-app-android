package org.p2p.wallet.settings.ui.recovery.userseedphrase

import org.p2p.uikit.organisms.seedphrase.SeedPhraseWord
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface UserSeedPhraseContract {
    interface View : MvpView {
        fun showSeedPhase(seedPhaseList: List<SeedPhraseWord>, isEditable: Boolean)
        fun copyToClipboard(seedPhase: String)
    }

    interface Presenter : MvpPresenter<View> {
        fun onCopyClicked()
        fun onBlurStateChanged(isBlurred: Boolean)
    }
}
