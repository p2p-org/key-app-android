package org.p2p.wallet.auth.ui.phone.countrypicker

import org.p2p.wallet.auth.ui.phone.model.CountryCode
import org.p2p.wallet.auth.ui.phone.model.CountryCodeAdapterItem
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface CountryPickerContract {

    interface View : MvpView {
        fun showCountries(items: List<CountryCodeAdapterItem>)
        fun setCountryCode(code: CountryCode)
    }

    interface Presenter : MvpPresenter<View> {
        fun search(name: String)
        fun load(countryCode: CountryCode?)
        fun onItemSelected(item: CountryCodeAdapterItem)
        fun onCountrySelected()
    }
}
