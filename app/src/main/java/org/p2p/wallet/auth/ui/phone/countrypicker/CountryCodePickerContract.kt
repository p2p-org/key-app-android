package org.p2p.wallet.auth.ui.phone.countrypicker

import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.auth.model.CountryCodeItem
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface CountryCodePickerContract {

    interface View : MvpView {
        fun showCountries(items: List<CountryCodeItem>)
    }

    interface Presenter : MvpPresenter<View> {
        fun search(text: String)
        fun load(countryCode: CountryCode?)
    }
}
