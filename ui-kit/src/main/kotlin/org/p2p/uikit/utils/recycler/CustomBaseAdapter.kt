package org.p2p.uikit.utils.recycler

import org.p2p.uikit.model.AnyCellItem

interface CustomBaseAdapter {
    fun getItems(): List<AnyCellItem>
}
