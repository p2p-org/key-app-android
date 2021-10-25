package org.p2p.wallet.restore.ui.derivable

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.restore.model.DerivableAccount
import org.p2p.solanaj.crypto.DerivationPath

interface DerivableAccountsContract {

    interface View : MvpView {
        fun navigateToCreatePin()
        fun showAccounts(path: DerivationPath, accounts: List<DerivableAccount>)
        fun showPathSelectionDialog(path: DerivationPath)
        fun showLoading(isLoading: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadData()
        fun setNewPath(path: DerivationPath)
        fun createAndSaveAccount()
        fun loadCurrentPath()
    }
}