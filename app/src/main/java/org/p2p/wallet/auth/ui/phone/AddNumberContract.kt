package org.p2p.wallet.auth.ui.phone

import org.p2p.wallet.auth.ui.phone.model.CountryCode
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface AddNumberContract {
    interface View : MvpView {
        fun showCountry(country: CountryCode?)
    }

    interface Presenter : MvpPresenter<View> {
        fun load()
    }
}
