package org.p2p.wallet.receive.tokenselect

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.p2p.core.token.Token
import org.p2p.core.token.TokenData
import org.p2p.core.utils.Constants
import org.p2p.ethereumkit.external.model.ERC20Tokens
import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.receive.tokenselect.ReceiveTokensMapper.toTokenFinanceCellModel
import org.p2p.wallet.receive.tokenselect.models.ReceiveNetwork
import org.p2p.wallet.receive.tokenselect.models.ReceiveTokenPayload
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.emptyString

private const val PAGE_SIZE = 20

class ReceiveTokensPresenter(
    private val interactor: UserInteractor,
    private val dispatchers: CoroutineDispatchers
) : BasePresenter<ReceiveTokensContract.View>(), ReceiveTokensContract.Presenter {

    private var searchText = emptyString()
    private var scrollToUp = false

    private var solToken: Token? = null
    private var ethToken: Token? = null

    private var lastSelectedTokenPayload: ReceiveTokenPayload? = null
    private var searchJob: Job? = null

    private val tokensFlow = MutableStateFlow<List<AnyCellItem>>(emptyList())
    private lateinit var pinnedWormholeTokens: List<TokenData>
    private lateinit var pinnedWormholeTokensAddresses: List<String>

    override fun attach(view: ReceiveTokensContract.View) {
        super.attach(view)
        launch {
            pinnedWormholeTokens = preparePinedWormholeTokens()
            pinnedWormholeTokensAddresses = pinnedWormholeTokens.map { it.mintAddress }
            val tokensForReceiveBanner = interactor.getTokensForBuy(
                availableTokensSymbols = listOf(
                    Constants.SOL_SYMBOL,
                    Constants.ETH_SYMBOL
                )
            )
            solToken = tokensForReceiveBanner[0]
            ethToken = tokensForReceiveBanner[1]
            view.setBannerTokens(
                firstTokenUrl = solToken?.iconUrl.orEmpty(),
                secondTokenUrl = ethToken?.iconUrl.orEmpty()
            )
            observeTokens()
            observeMappedTokens()
        }
    }

    private fun preparePinedWormholeTokens(): List<TokenData> {
        return ERC20Tokens.values().mapNotNull { erc20Token ->
            interactor.findTokenDataByAddress(erc20Token.mintAddress)?.let { token ->
                TokenData(
                    mintAddress = token.mintAddress,
                    name = erc20Token.replaceTokenName ?: token.tokenName,
                    symbol = erc20Token.replaceTokenSymbol ?: token.tokenSymbol,
                    iconUrl = erc20Token.tokenIconUrl ?: token.iconUrl,
                    decimals = token.decimals,
                    coingeckoId = erc20Token.coingeckoId,
                    isWrapped = false,
                    serumV3Usdc = null,
                    serumV3Usdt = null
                )
            }
        }
    }

    override fun load(isRefresh: Boolean, scrollToUp: Boolean) {
        launch {
            if (isRefresh) {
                tokensFlow.value = emptyList()
            }
            this@ReceiveTokensPresenter.scrollToUp = scrollToUp
            interactor.fetchTokens(searchText, PAGE_SIZE, isRefresh)
        }
    }

    override fun onSearchTokenQueryChanged(newQuery: String) {
        searchJob?.cancel()
        searchJob = launch {
            delay(300L)
            searchText = newQuery
            if (newQuery.isBlank()) {
                view?.resetView()
                tokensFlow.value = emptyList()
            }
            load(isRefresh = true, scrollToUp = true)
        }
    }

    override fun onTokenClicked(tokenDataPayload: ReceiveTokenPayload) {
        if (tokenDataPayload.isErc20Token) {
            lastSelectedTokenPayload = tokenDataPayload
            view?.showSelectNetworkDialog(listOfNotNull(solToken, ethToken))
        } else {
            view?.openReceiveInSolana(tokenDataPayload.tokenData)
        }
    }

    override fun onNetworkSelected(network: ReceiveNetwork) {
        lastSelectedTokenPayload?.tokenData?.let { tokenData ->
            view?.apply {
                when (network) {
                    ReceiveNetwork.SOLANA -> openReceiveInSolana(tokenData)
                    ReceiveNetwork.ETHEREUM -> openReceiveInEthereum(tokenData)
                }
            }
        }
    }

    private fun observeMappedTokens() {
        launch {
            tokensFlow.collectLatest {
                view?.showTokenItems(it)
            }
        }
    }

    private fun observeTokens() {
        launch {
            interactor.getTokenListFlow().distinctUntilChanged().collectLatest { data ->
                val isEmpty = data.result.isEmpty()
                view?.showEmptyState(isEmpty)
                view?.setBannerVisibility(!isEmpty && searchText.isEmpty())

                val isPinnedItemsNeeded = searchText.isEmpty()
                launch {
                    val dropSize = tokensFlow.value.size
                    val oldItems = if (isPinnedItemsNeeded && dropSize == 0) {
                        tokensFlow.value + mapTokenToCellItem(pinnedWormholeTokens)
                    } else {
                        tokensFlow.value
                    }

                    val newItems = data.result.drop(dropSize).toMutableList().let { list ->
                        if (isPinnedItemsNeeded) list.filter { it.mintAddress !in pinnedWormholeTokensAddresses }
                        else list
                    }

                    val result = oldItems + mapTokenToCellItem(newItems)
                    tokensFlow.emit(result)
                }
            }
        }
    }

    private suspend fun mapTokenToCellItem(items: List<TokenData>): List<AnyCellItem> {
        return withContext(dispatchers.io) {
            items.map {
                it.toTokenFinanceCellModel(
                    solTokenUrl = solToken?.iconUrl.orEmpty(),
                    ethTokenUrl = ethToken?.iconUrl.orEmpty()
                )
            }
        }
    }
}
