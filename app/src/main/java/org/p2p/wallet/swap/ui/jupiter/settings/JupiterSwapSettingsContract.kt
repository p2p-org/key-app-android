package org.p2p.wallet.swap.ui.jupiter.settings

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface JupiterSwapSettingsContract {
    interface View : MvpView

    interface Presenter : MvpPresenter<View>
}
