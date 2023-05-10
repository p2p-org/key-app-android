package org.p2p.wallet.home.ui.new

import kotlin.properties.Delegates.observable
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.p2p.core.token.Token
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.home.model.SelectTokenItem
import org.p2p.wallet.home.model.SelectTokenItem.CategoryTitle
import org.p2p.wallet.home.model.SelectTokenItem.SelectableToken
import org.p2p.wallet.home.model.SelectableTokenRoundedState.BOTTOM_ROUNDED
import org.p2p.wallet.home.model.SelectableTokenRoundedState.NOT_ROUNDED
import org.p2p.wallet.home.model.SelectableTokenRoundedState.ROUNDED
import org.p2p.wallet.home.model.SelectableTokenRoundedState.TOP_ROUNDED
import org.p2p.wallet.home.repository.UserTokensRepository

class NewSelectTokenPresenter(
    private val userTokensRepository: UserTokensRepository,
    private val selectedTokenMintAddress: String?,
    private val selectableTokens: List<Token.Active>?
) : BasePresenter<NewSelectTokenContract.View>(), NewSelectTokenContract.Presenter {

    private var mappedTokens: List<SelectTokenItem> by observable(emptyList()) { _, _, new ->
        view?.showTokens(new)
        view?.showEmptyState(isVisible = new.isEmpty())
    }

    override fun attach(view: NewSelectTokenContract.View) {
        super.attach(view)

        userTokensRepository.observeUserTokens()
            .map(::getTokensToSelect)
            .onEach(::initTokensToSelect)
            .launchIn(this)
    }

    private fun getTokensToSelect(userTokens: List<Token.Active>): List<Token.Active> {
        return if (selectableTokens != null) {
            userTokens.filter { it.mintAddress in selectableTokens.map(Token.Active::mintAddress) }
        } else {
            userTokens
        }.filterNot(Token.Active::isZero)
    }

    private fun initTokensToSelect(tokensToSelect: List<Token.Active>) {
        val selectedToken = tokensToSelect.firstOrNull { it.mintAddress == selectedTokenMintAddress }
        mappedTokens = mapInitialTokens(selectedToken, tokensToSelect)
    }

    override fun search(tokenNameQuery: String) {
        // restoring the list text is too short
        if (tokenNameQuery.isEmpty()) {
            view?.showTokens(mappedTokens)
            view?.showEmptyState(isVisible = mappedTokens.isEmpty())
            view?.scrollToTop()
            return
        }

        // start searching
        val filteredItems = mappedTokens
            .filterIsInstance<SelectableToken>()
            .filter { item ->
                val tokenName = item.token.tokenName.lowercase()
                val tokenSymbol = item.token.tokenSymbol.lowercase()

                tokenName.contains(tokenNameQuery) || tokenSymbol.contains(tokenNameQuery)
            }
            .toMutableList()

        @Suppress("UNCHECKED_CAST")
        val updatedRoundStates = updateRoundedStates(filteredItems as List<SelectableToken>)

        if (updatedRoundStates.isEmpty()) {
            view?.clearTokens()
            view?.showEmptyState(isVisible = true)
        } else {
            updatedRoundStates.add(0, CategoryTitle(R.string.send_pick_token_search_result_text))
            view?.showTokens(updatedRoundStates)
            view?.showEmptyState(isVisible = false)
        }
    }

    private fun mapInitialTokens(
        selectedToken: Token.Active?,
        tokens: List<Token.Active>
    ): List<SelectTokenItem> = buildList {
        if (selectedToken != null) {
            this += CategoryTitle(R.string.send_pick_token_chosen)
            this += SelectableToken(selectedToken, ROUNDED)
        }

        val otherTokens = tokens.filterNot { it.publicKey == selectedToken?.publicKey }
        if (otherTokens.isNotEmpty()) {
            this += CategoryTitle(R.string.send_pick_token_other)

            val otherTokensSize = otherTokens.size
            this += otherTokens.mapIndexed { index, token ->
                if (otherTokensSize == 1) {
                    return@mapIndexed SelectableToken(token, ROUNDED)
                }

                val roundedState = when (index) {
                    0 -> TOP_ROUNDED
                    otherTokensSize - 1 -> BOTTOM_ROUNDED
                    else -> NOT_ROUNDED
                }

                SelectableToken(token, roundedState)
            }
        }
    }

    /*
    * Updating corners when the list is filtered according to the search query
    * In this case every viewHolder should update its corners radius or remove it if needed
    * */
    private fun updateRoundedStates(mappedTokens: List<SelectableToken>): MutableList<SelectTokenItem> {
        val mappedTokensSize = mappedTokens.size

        val updatedList = mutableListOf<SelectTokenItem>()

        mappedTokens.forEachIndexed { index, item ->
            if (mappedTokensSize == 1) {
                updatedList += SelectableToken(item.token, ROUNDED)
                return@forEachIndexed
            }

            val roundedState = when (index) {
                0 -> TOP_ROUNDED
                mappedTokensSize - 1 -> BOTTOM_ROUNDED
                else -> NOT_ROUNDED
            }

            updatedList += SelectableToken(item.token, roundedState)
            return@forEachIndexed
        }

        return updatedList
    }
}
