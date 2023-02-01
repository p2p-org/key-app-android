package org.p2p.wallet.deeplinks

import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton

interface MainScreenActionButtonHandler {
    fun onActionButtonClicked(clickedButton: ActionButton)
}
