package org.p2p.wallet.settings.ui.recovery.seed

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface SeedPhraseContract {
    interface View : MvpView {
        fun showSeedPhrase(seedPhrase: List<String>)
    }

    interface Presenter : MvpPresenter<View>
}
