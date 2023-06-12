package org.p2p.uikit.organisms.sectionheader

import org.p2p.core.common.TextContainer
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.model.CellModelPayload

data class SectionHeaderCellModel(
    val sectionTitle: TextContainer,
    val isShevronVisible: Boolean,
    override val payload: CellModelPayload? = null,
    val backgroundColor: Int? = null,
    val textColor: Int? = null,
    val textAppearance: Int? = null
    // todo: add shevron container
) : CellModelPayload, AnyCellItem
