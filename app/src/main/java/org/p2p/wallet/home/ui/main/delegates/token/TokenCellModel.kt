package org.p2p.wallet.home.ui.main.delegates.token

import org.p2p.core.token.Token
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.model.CellModelPayload
import org.p2p.uikit.utils.text.TextViewCellModel

data class TokenCellModel(
    val iconUrl: String?,
    val tokenName: String,
    val isWrapped: Boolean,
    val formattedUsdTotal: String?,
    val formattedTotal: String?,
    val formattedPnl: TextViewCellModel?,
    val isDefinitelyHidden: Boolean,
    override val payload: Token.Active,
) : CellModelPayload, AnyCellItem
