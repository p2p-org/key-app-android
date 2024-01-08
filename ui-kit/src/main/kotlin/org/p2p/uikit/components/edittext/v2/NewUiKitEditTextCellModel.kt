package org.p2p.uikit.components.edittext.v2

import androidx.annotation.DrawableRes
import org.p2p.uikit.model.AnyCellItem

class NewUiKitEditTextCellModel(
    val text: CharSequence,
    @DrawableRes
    val endIcon: Int? = null,
    val isErrorState: Boolean,
) : AnyCellItem
