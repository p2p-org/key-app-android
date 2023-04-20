package org.p2p.wallet.restore.ui.derivable

import org.p2p.solanaj.crypto.DerivationPath
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.restore.model.DerivableAccount

interface DerivableAccountsContract {

    interface View : MvpView {
        fun navigateToCreatePin()
        fun showAccounts(accounts: List<DerivableAccount>)
        fun showLoading(isLoading: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadData()
        fun setNewPath(path: DerivationPath)
        fun createAndSaveAccount(walletIndex: Int = 0)
    }
}
