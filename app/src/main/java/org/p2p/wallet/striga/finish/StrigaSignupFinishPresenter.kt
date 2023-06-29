package org.p2p.wallet.striga.finish

import kotlinx.coroutines.launch
import org.p2p.core.common.di.AppScope
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.wallet.striga.user.interactor.StrigaUserInteractor

class StrigaSignupFinishPresenter(
    private val strigaUserInteractor: StrigaUserInteractor,
    appScope: AppScope,
    dispatchers: CoroutineDispatchers,
) : BasePresenter<MvpView>(dispatchers.ui), StrigaSignupFinishContract.Presenter {

    init {
        appScope.launch(dispatchers.io) {
            strigaUserInteractor.loadAndSaveUserStatusData()
        }
    }
}
