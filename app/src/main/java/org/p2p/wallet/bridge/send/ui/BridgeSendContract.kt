package org.p2p.wallet.bridge.send.ui

import java.math.BigDecimal
import org.p2p.core.token.Token
import org.p2p.uikit.components.UiKitSendDetailsWidgetContract
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface BridgeSendContract {
    interface View : MvpView

    interface Presenter : MvpPresenter<View> {
        fun attach(view: UiKitSendDetailsWidgetContract)
        fun updateToken(newToken: Token.Active)
        fun updateAmount(newAmount: BigDecimal)
    }
}
