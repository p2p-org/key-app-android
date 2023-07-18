package org.p2p.wallet.striga.onramp.iban.presenter

import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.striga.onramp.iban.StrigaUserIbanDetailsContract
import org.p2p.wallet.striga.wallet.interactor.StrigaWalletInteractor

class StrigaUserIbanDetailsPresenter(
    private val interactor: StrigaWalletInteractor,
    private val mapper: StrigaUserIbanUiMapper,
    dispatchers: CoroutineDispatchers
) : BasePresenter<StrigaUserIbanDetailsContract.View>(dispatchers.ui),
    StrigaUserIbanDetailsContract.Presenter {

    override fun attach(view: StrigaUserIbanDetailsContract.View) {
        super.attach(view)

        launch {
            try {
                val ibanDetails = interactor.getFiatAccountDetails()
                view.showIbanDetails(mapper.mapToCellModels(ibanDetails))
            } catch (error: Throwable) {
                Timber.e(error, "Failed to get iban details")
                view.showUiKitSnackBar(messageResId = R.string.error_general_message)
                view.navigateBack()
            }
        }
    }
}
