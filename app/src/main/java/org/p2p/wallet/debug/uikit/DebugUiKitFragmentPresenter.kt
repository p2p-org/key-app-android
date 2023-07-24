package org.p2p.wallet.debug.uikit

import org.p2p.uikit.components.edittext.v2.NewUiKitEditTextCellModel
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BasePresenter

class DebugUiKitFragmentPresenter :
    BasePresenter<DebugUiKitFragmentContract.View>(),
    DebugUiKitFragmentContract.Presenter {
    private val informerViewBuilder = DebugInformerViewBuilder()

    override fun buildInformerViews() {
        view?.showViews(
            informerViewBuilder.build(
                onInfoLineClicked = { view?.showUiKitSnackBar("informer view clicked") }
            ) + listOf(
                NewUiKitEditTextCellModel(
                    text = "Text 1",
                    endIcon = null,
                    isErrorState = false
                ),
                NewUiKitEditTextCellModel(
                    text = "Text 2",
                    endIcon = R.drawable.ic_close,
                    isErrorState = false
                ),
                NewUiKitEditTextCellModel(
                    text = "Text 3",
                    endIcon = null,
                    isErrorState = true
                )
            )
        )
    }
}
