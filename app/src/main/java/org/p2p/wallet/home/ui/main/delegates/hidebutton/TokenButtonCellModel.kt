package org.p2p.wallet.home.ui.main.delegates.hidebutton

import androidx.annotation.DrawableRes
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.model.CellModelPayload
import org.p2p.wallet.home.model.VisibilityState

data class TokenButtonCellModel(
    @DrawableRes val visibilityIcon: Int,
    override val payload: VisibilityState,
) : CellModelPayload, AnyCellItem
