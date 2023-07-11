package org.p2p.wallet.home.ui.main.delegates.bridgeclaim

import androidx.annotation.ColorRes
import org.p2p.core.token.Token
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.model.CellModelPayload

data class ClaimTokenCellModel(
    val iconUrl: String?,
    val tokenName: String,
    val formattedTotal: String?,
    val buttonText: String,
    @ColorRes val buttonTextColor: Int,
    @ColorRes val buttonBackgroundColor: Int,
    val isClaimEnabled: Boolean,
    override val payload: Token.Eth,
) : CellModelPayload, AnyCellItem
