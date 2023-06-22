package org.p2p.wallet.common.ui.widget

import android.view.ViewGroup
import com.google.android.material.snackbar.BaseTransientBottomBar

@Deprecated("use uikit snackbar")
class SnackBarView(
    parent: ViewGroup,
    content: CustomSnackBar
) : BaseTransientBottomBar<SnackBarView>(parent, content, content) {

    init {
        view.setBackgroundColor(context.getColor(android.R.color.transparent))
        view.setPadding(0, 0, 0, 0)
    }
}
