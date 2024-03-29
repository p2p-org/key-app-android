package org.p2p.wallet.auth.ui.restore.common

import org.p2p.wallet.auth.model.GatewayHandledState
import org.p2p.wallet.auth.model.RestoreFailureState
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface CommonRestoreContract {
    interface View : MvpView {
        fun startGoogleFlow()
        fun navigateToPinCreate()
        fun showError(error: String)
        fun onNoTokenFoundError(userId: String)
        fun navigateToPhoneEnter()
        fun setLoadingState(isScreenLoading: Boolean)
        fun setRestoreViaGoogleFlowVisibility(isVisible: Boolean)
        fun showGeneralErrorScreen(handledState: GatewayHandledState)
        fun showRestoreErrorScreen(handledState: RestoreFailureState.TitleSubtitleError)
    }

    interface Presenter : MvpPresenter<View> {
        fun useGoogleAccount()
        fun useCustomShare()
        fun setGoogleIdToken(userId: String, idToken: String)
        fun switchFlowToRestore()
        fun useSeedPhrase()
    }
}
