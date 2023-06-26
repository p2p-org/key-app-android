package org.p2p.wallet.debug.uikit

import org.p2p.wallet.common.mvp.BasePresenter

class DebugUiKitFragmentPresenter :
    BasePresenter<DebugUiKitFragmentContract.View>(),
    DebugUiKitFragmentContract.Presenter {
    private val informerViewBuilder = DebugInformerViewBuilder()

    override fun buildInformerViews() {
        view?.showViews(
            informerViewBuilder.build(
                onInfoLineClicked = { view?.showUiKitSnackBar("informer view clicked") }
            )
        )
    }
}
