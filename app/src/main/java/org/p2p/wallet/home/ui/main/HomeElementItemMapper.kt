package org.p2p.wallet.home.ui.main

import kotlinx.coroutines.withContext
import org.p2p.core.token.Token
import org.p2p.wallet.R
import org.p2p.wallet.home.model.HomeElementItem
import org.p2p.wallet.home.model.VisibilityState
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers

class HomeElementItemMapper(
    private val dispatchers: CoroutineDispatchers,
) {

    suspend fun mapToItems(
        tokens: List<Token.Active>,
        ethereumTokens: List<Token.Eth>,
        visibilityState: VisibilityState,
        isZerosHidden: Boolean,
    ): List<HomeElementItem> = withContext(dispatchers.io) {

        val groups: Map<Boolean, List<Token.Active>> = tokens.groupBy { token ->
            token.isDefinitelyHidden(isZerosHidden)
        }

        val hiddenTokens = groups[true].orEmpty()
        val visibleTokens = groups[false].orEmpty()

        val result = mutableListOf<HomeElementItem>(HomeElementItem.Title(R.string.home_tokens))

        result += ethereumTokens.map { token ->
            HomeElementItem.Claim(
                token = token,
                isClaimEnabled = !token.isClaiming
            )
        }

        result += visibleTokens.map { HomeElementItem.Shown(it) }

        if (hiddenTokens.isNotEmpty()) {
            result += HomeElementItem.Action(visibilityState)
        }

        if (visibilityState.isVisible) {
            result += hiddenTokens.map { HomeElementItem.Hidden(it, visibilityState) }
        }

        return@withContext result
    }
}
