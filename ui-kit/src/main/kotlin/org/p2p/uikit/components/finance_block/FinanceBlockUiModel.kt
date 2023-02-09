package org.p2p.uikit.components.finance_block

import org.p2p.uikit.components.left_side.LeftSideUiModel
import org.p2p.uikit.components.right_side.RightSideUiModel
import org.p2p.uikit.model.UiModelPayload
import org.p2p.uikit.utils.viewState.ViewAvailabilityUiModel

data class FinanceBlockUiModel(
    val leftSideUiModel: LeftSideUiModel? = null,
    val rightSideUiModel: RightSideUiModel? = null,
    val availability: ViewAvailabilityUiModel = ViewAvailabilityUiModel(),
    override val payload: Any? = null,
) : UiModelPayload
