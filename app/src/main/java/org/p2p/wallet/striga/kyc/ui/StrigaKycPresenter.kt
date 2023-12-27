package org.p2p.wallet.striga.kyc.ui

import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.core.common.di.AppScope
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.wallet.alarmlogger.logger.AlarmErrorsLogger
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.striga.kyc.interactor.StrigaKycInteractor
import org.p2p.wallet.striga.user.interactor.StrigaUserInteractor

class StrigaKycPresenter(
    private val strigaKycInteractor: StrigaKycInteractor,
    private val strigaUserInteractor: StrigaUserInteractor,
    private val appScope: AppScope,
    private val dispatchers: CoroutineDispatchers,
    private val alarmErrorsLogger: AlarmErrorsLogger
) : BasePresenter<StrigaKycContract.View>(), StrigaKycContract.Presenter {

    private fun updateUserStatus() {
        appScope.launch(dispatchers.io) {
            // update user status when kyc/start called
            kotlin.runCatching { strigaUserInteractor.loadAndSaveUserStatusData().unwrap() }
                .onFailure { Timber.e(it, "Unable to load striga user status") }
        }
    }
}
