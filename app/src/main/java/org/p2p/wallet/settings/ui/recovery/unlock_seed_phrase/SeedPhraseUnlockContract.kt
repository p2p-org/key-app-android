package org.p2p.wallet.settings.ui.recovery.unlock_seed_phrase

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface SeedPhraseUnlockContract {
    interface View : MvpView

    interface Presenter : MvpPresenter<View>
}
