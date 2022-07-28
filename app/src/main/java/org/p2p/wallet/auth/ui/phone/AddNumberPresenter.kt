package org.p2p.wallet.auth.ui.phone

import android.content.Context
import org.p2p.wallet.auth.ui.phone.model.CountryCode
import org.p2p.wallet.common.mvp.BasePresenter

class AddNumberPresenter(
    private val countryCodeInteractor: CountryCodeInteractor,
    private val context: Context
) : BasePresenter<AddNumberContract.View>(), AddNumberContract.Presenter {

    override fun load() {
        val countryCode: CountryCode? =
            countryCodeInteractor.detectSimCountry(context)
                ?: countryCodeInteractor.detectNetworkCountry(context)
                ?: countryCodeInteractor.detectLocaleCountry(context.resources)

        view?.showCountry(countryCode)
    }
}
