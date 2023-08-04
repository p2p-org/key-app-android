package org.p2p.wallet.striga.onramp.iban.presenter

import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.striga.onramp.iban.StrigaUserIbanDetailsContract
import org.p2p.wallet.striga.onramp.interactor.StrigaOnRampInteractor
import org.p2p.wallet.striga.wallet.interactor.StrigaWalletInteractor

class StrigaUserIbanDetailsPresenter(
    private val interactor: StrigaOnRampInteractor,
    private val walletInteractor: StrigaWalletInteractor,
    private val mapper: StrigaUserIbanUiMapper,
    dispatchers: CoroutineDispatchers
) : BasePresenter<StrigaUserIbanDetailsContract.View>(dispatchers.ui),
    StrigaUserIbanDetailsContract.Presenter {

    override fun firstAttach() {
        super.firstAttach()
        if (!interactor.isIbanNotesHidden) {
            view?.showImportantNotes(checkBoxIsChecked = false)
        }
    }

    override fun attach(view: StrigaUserIbanDetailsContract.View) {
        super.attach(view)

        launch {
            try {
                val ibanDetails = walletInteractor.getFiatAccountDetails()
                view.showIbanDetails(mapper.mapToCellModels(ibanDetails))
                view.showImportantNotesInformer(
                    mapper.mapImportantNotesInformerModel(ibanDetails)
                )
            } catch (error: Throwable) {
                Timber.e(error, "Failed to get iban details")
                view.showUiKitSnackBar(messageResId = R.string.error_general_message)
                view.navigateBack()
            }
        }
    }

    override fun onImportantNotesInformerClick() {
        view?.showImportantNotes(checkBoxIsChecked = interactor.isIbanNotesHidden)
    }
}
