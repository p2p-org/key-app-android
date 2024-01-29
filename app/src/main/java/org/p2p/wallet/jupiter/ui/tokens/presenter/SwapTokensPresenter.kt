package org.p2p.wallet.jupiter.ui.tokens.presenter

import timber.log.Timber
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.core.crypto.toBase58Instance
import org.p2p.uikit.components.finance_block.MainCellModel
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.jupiter.interactor.SwapTokensInteractor
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.jupiter.ui.tokens.SwapTokensContract
import org.p2p.wallet.jupiter.ui.tokens.SwapTokensListMode

class SwapTokensPresenter(
    private val tokenToChange: SwapTokensListMode,
    private val mapperA: SwapTokensAMapper,
    private val mapperB: SwapTokensBMapper,
    private val searchResultMapper: SearchSwapTokensMapper,
    private val interactor: SwapTokensInteractor,
) : BasePresenter<SwapTokensContract.View>(), SwapTokensContract.Presenter {

    private var allTokens: List<SwapTokenModel> = emptyList()
    private var currentToken: SwapTokenModel? = null
    private var searchJob: Job? = null
    private var loadAllTokensJob: Job? = null

    override fun attach(view: SwapTokensContract.View) {
        super.attach(view)
        loadAllTokensJob = launch {
            renderLoading(isLoading = true)
            try {
                renderAllSwapTokensList()
            } catch (cancelled: CancellationException) {
                Unit
            } catch (error: Throwable) {
                Timber.e(error, "Failed to load swap tokens")
                view.showUiKitSnackBar(messageResId = R.string.error_general_message)
            }
            renderLoading(isLoading = false)
        }
    }

    private suspend fun loadAllTokens() {
        val currentTokenToSwapTokens = when (tokenToChange) {
            SwapTokensListMode.TOKEN_A -> interactor.requireCurrentTokenA() to interactor.getAllTokensA()
            SwapTokensListMode.TOKEN_B -> interactor.getCurrentTokenB() to interactor.getAllAvailableTokensB()
        }
        currentToken = currentTokenToSwapTokens.first
        allTokens = currentTokenToSwapTokens.second
    }

    private suspend fun renderAllSwapTokensList() {
        if (allTokens.isEmpty()) {
            loadAllTokens()
        }
        val cellItems = when (tokenToChange) {
            SwapTokensListMode.TOKEN_A -> {
                mapperA.toTokenACellItems(
                    chosenToken = currentToken ?: return,
                    swapTokens = allTokens
                )
            }
            SwapTokensListMode.TOKEN_B -> {
                mapperB.toTokenBCellItems(
                    selectedTokenModel = currentToken ?: return,
                    tokens = allTokens
                )
            }
        }
        // todo: optimize. There are about 800~ items being set at once,
        //  we need to set list partially
        view?.setTokenItems(cellItems)
        view?.showEmptyState(isEmpty = false)
    }

    override fun onSearchTokenQueryChanged(newQuery: String) {
        // cancel the initial job that can override result from searchJob
        loadAllTokensJob?.cancel()
        searchJob?.cancel()
        searchJob = launch {
            renderLoading(isLoading = true)
            delay(1000)
            if (newQuery.isBlank()) {
                renderAllSwapTokensList()
            } else {
                renderSearchTokenList(newQuery)
            }
            renderLoading(isLoading = false)
        }
    }

    private suspend fun renderSearchTokenList(newQuery: String) {
        try {
            val searchResult = interactor.searchToken(tokenToChange, newQuery)
            val cellItems = searchResultMapper.toCellItems(searchResult)
            view?.setTokenItems(cellItems)

            val isEmpty = cellItems.none { it is MainCellModel }
            view?.showEmptyState(isEmpty = isEmpty)
        } catch (error: Throwable) {
            view?.setTokenItems(emptyList())
            view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
        }
    }

    override fun onTokenClicked(clickedToken: SwapTokenModel) {
        interactor.selectToken(tokenToChange, clickedToken)
        view?.close()
    }

    override fun onNonStrictTokenConfirmed(clickedTokenMint: String) {
        launch {
            interactor.findTokenByMintAddress(clickedTokenMint.toBase58Instance())
                ?.also { onTokenClicked(it) }
        }
    }

    private fun renderLoading(isLoading: Boolean) {
        view?.renderLoading(isLoading)
    }
}
