package org.p2p.wallet.home.ui.main.delegates.striga.offramp

import java.math.BigDecimal
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.model.CellModelPayload
import org.p2p.wallet.striga.offramp.models.StrigaOffRampToken

data class StrigaOffRampCellModel(
    val amountAvailable: BigDecimal,
    val tokenSymbol: String,
    val isLoading: Boolean,
    override val payload: StrigaOffRampToken,
) : CellModelPayload, AnyCellItem
