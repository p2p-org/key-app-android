package org.p2p.wallet.auth.ui.phone.countrypicker

import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.auth.model.CountryCodeItem
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface CountryCodePickerContract {

    interface View : MvpView {
        fun showCountries(items: List<CountryCodeItem>)
        fun setCountryCode(code: CountryCode)
    }

    interface Presenter : MvpPresenter<View> {
        fun searchByCountryName(name: String)
        fun load(countryCode: CountryCode?)
        fun onItemSelected(item: CountryCodeItem)
        fun onCountryCodeSelected()
    }
}
