package org.p2p.wallet.home.ui.main.delegates.striga.onramp

import java.math.BigDecimal
import org.p2p.core.crypto.Base58String
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.model.CellModelPayload
import org.p2p.wallet.striga.onramp.interactor.StrigaOnRampToken

data class StrigaOnRampCellModel(
    val amountAvailable: BigDecimal,
    val tokenName: String,
    val tokenMintAddress: Base58String,
    val tokenSymbol: String,
    val tokenIcon: String,
    val isLoading: Boolean,
    override val payload: StrigaOnRampToken,
) : CellModelPayload, AnyCellItem
