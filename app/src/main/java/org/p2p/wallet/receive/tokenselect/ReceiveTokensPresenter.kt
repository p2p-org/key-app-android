package org.p2p.wallet.receive.tokenselect

import androidx.annotation.StringRes
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.p2p.core.common.TextContainer
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.token.TokenMetadata
import org.p2p.core.token.TokenMetadataExtension
import org.p2p.core.token.filterByAvailability
import org.p2p.ethereumkit.external.model.ERC20Tokens
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.organisms.sectionheader.SectionHeaderCellModel
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BasePresenter
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

    private var lastSelectedTokenPayload: ReceiveTokenPayload? = null
    private var searchJob: Job? = null

    private val tokensFlow = MutableStateFlow<List<AnyCellItem>>(emptyList())
    private lateinit var pinnedWormholeTokens: List<TokenMetadata>
    private lateinit var pinnedWormholeTokensAddresses: List<String>

    override fun attach(view: ReceiveTokensContract.View) {
        super.attach(view)
        launch {
            pinnedWormholeTokens = preparePinedWormholeTokens()
            pinnedWormholeTokensAddresses = pinnedWormholeTokens.map { it.mintAddress }
            view.setBannerTokens(
                firstTokenUrl = ERC20Tokens.SOL_TOKEN_URL,
                secondTokenUrl = ERC20Tokens.ETH.tokenIconUrl.orEmpty()
            )
            observeTokens()
            observeMappedTokens()
        }
    }

    private suspend fun preparePinedWormholeTokens(): List<TokenMetadata> {
        return ERC20Tokens.values()
            .mapNotNull { erc20Token ->
                interactor.findTokenDataByAddress(erc20Token.mintAddress)?.let { token ->
                    TokenMetadata(
                        mintAddress = token.mintAddress,
                        name = erc20Token.replaceTokenName ?: token.tokenName,
                        symbol = erc20Token.replaceTokenSymbol ?: token.tokenSymbol,
                        iconUrl = erc20Token.tokenIconUrl,
                        decimals = token.decimals,
                        isWrapped = false,
                        extensions = TokenMetadataExtension.NONE
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
            view?.showSelectNetworkDialog()
        } else {
            view?.openReceiveInSolana(tokenDataPayload.tokenMetadata)
        }
    }

    override fun onNetworkSelected(network: ReceiveNetwork) {
        lastSelectedTokenPayload?.tokenMetadata?.let { tokenData ->
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

                val isSearching = searchText.isNotEmpty()
                val dropSize = tokensFlow.value.size
                val oldItems = if (!isSearching && dropSize == 0) {
                    tokensFlow.value + mapTokenToCellItem(pinnedWormholeTokens)
                } else {
                    tokensFlow.value
                }

                val newItems = data.result
                    .drop(dropSize)
                    .takeIf { !isSearching }
                    ?.filter { it.mintAddress !in pinnedWormholeTokensAddresses }
                    ?: data.result

                val startItems = if (isSearching && !oldItems.any { it is SectionHeaderCellModel }) {
                    listOf(
                        createSectionHeader(R.string.receive_token_search_found_header),
                        *oldItems.toTypedArray()
                    )
                } else {
                    oldItems
                }
                val result = startItems + mapTokenToCellItem(newItems)
                tokensFlow.emit(result)
            }
        }
    }

    private fun createSectionHeader(@StringRes stringRes: Int): SectionHeaderCellModel {
        return SectionHeaderCellModel(
            sectionTitle = TextContainer(stringRes),
            isShevronVisible = false
        )
    }

    private suspend fun mapTokenToCellItem(items: List<TokenMetadata>): List<AnyCellItem> {
        return withContext(dispatchers.io) {
            items.filterByAvailability().map {
                it.toTokenFinanceCellModel(
                    solTokenUrl = ERC20Tokens.SOL_TOKEN_URL,
                    ethTokenUrl = ERC20Tokens.ETH.tokenIconUrl.orEmpty()
                )
            }
        }
    }
}
