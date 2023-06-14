package org.p2p.wallet.settings.ui.mail

import kotlinx.coroutines.launch
import org.p2p.wallet.auth.interactor.restore.RestoreWalletInteractor
import org.p2p.wallet.common.mvp.BasePresenter

class SettingsEmailConfirmPresenter(
    private val restoreWalletInteractor: RestoreWalletInteractor,
) : BasePresenter<SettingsEmailConfirmContract.View>(), SettingsEmailConfirmContract.Presenter {

    override fun setGoogleIdToken(userId: String, idToken: String) {
        launch {
            view?.setLoadingState(isScreenLoading = true)
            restoreWalletInteractor.obtainTorusKey(userId = userId, idToken = idToken)
            // TODO update deviceShare
            view?.setLoadingState(isScreenLoading = false)
        }
    }
}
