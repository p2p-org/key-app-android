package org.p2p.uikit.organisms.sectionheader

import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.model.CellModelPayload
import org.p2p.uikit.utils.text.TextViewCellModel

data class SectionHeaderCellModel(
    val sectionTitle: TextViewCellModel,
    val isShevronVisible: Boolean,
    override val payload: CellModelPayload? = null
    // todo: add shevron container
): CellModelPayload, AnyCellItem
