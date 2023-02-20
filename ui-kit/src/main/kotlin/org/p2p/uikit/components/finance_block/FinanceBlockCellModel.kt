package org.p2p.uikit.components.finance_block

import org.p2p.uikit.components.left_side.LeftSideCellModel
import org.p2p.uikit.components.right_side.RightSideCellModel
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.model.CellModelPayload
import org.p2p.uikit.utils.viewState.ViewAccessibilityCellModel

data class FinanceBlockCellModel(
    val leftSideCellModel: LeftSideCellModel? = null,
    val rightSideCellModel: RightSideCellModel? = null,
    val accessibility: ViewAccessibilityCellModel = ViewAccessibilityCellModel(),
    override val payload: Any? = null,
) : CellModelPayload, AnyCellItem
