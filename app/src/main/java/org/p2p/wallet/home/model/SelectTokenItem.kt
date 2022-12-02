package org.p2p.wallet.home.model

import androidx.annotation.StringRes

sealed interface SelectTokenItem {

    data class CategoryTitle(@StringRes val titleRes: Int) : SelectTokenItem

    data class SelectableToken(val token: Token.Active, val state: SelectableTokenRoundedState) : SelectTokenItem
}
