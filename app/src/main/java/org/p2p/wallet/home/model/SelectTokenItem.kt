package org.p2p.wallet.home.model

import androidx.annotation.StringRes
import org.p2p.core.token.Token

sealed interface SelectTokenItem {

    data class CategoryTitle(@StringRes val titleRes: Int) : SelectTokenItem

    data class SelectableToken(val token: Token.Active, val state: SelectableTokenRoundedState) : SelectTokenItem
}
