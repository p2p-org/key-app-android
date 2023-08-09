package org.p2p.wallet.striga.signup.presetpicker

import org.p2p.core.common.DrawableContainer

data class SelectableItem(
    val id: String,
    val itemIcon: DrawableContainer?,
    val itemEmoji: String?,
    val itemTitle: String,
    val itemSubtitle: String?,
)
