package org.p2p.wallet.striga.onramp.iban.presenter

import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.striga.onramp.iban.StrigaIbanImportantNotesContract
import org.p2p.wallet.striga.onramp.interactor.StrigaOnRampInteractor

class StrigaIbanImportantNotesDialogPresenter(
    private val onRampInteractor: StrigaOnRampInteractor,
    private val mapper: StrigaUserIbanUiMapper,
) : BasePresenter<StrigaIbanImportantNotesContract.View>(),
    StrigaIbanImportantNotesContract.Presenter {

    private val notes = listOf(
        mapper.mapImportantNotesCellModel(
            textRes = R.string.striga_iban_account_dialog_note_first,
            iconRes = R.drawable.ic_user
        ),
        mapper.mapImportantNotesCellModel(
            textRes = R.string.striga_iban_account_dialog_note_second,
            iconRes = R.drawable.ic_bank
        )
    )

    override fun attach(view: StrigaIbanImportantNotesContract.View) {
        super.attach(view)
        view.showNotes(notes)
    }

    override fun onDontShowAgainChecked(isChecked: Boolean) {
        onRampInteractor.isIbanNotesHidden = isChecked
    }
}
