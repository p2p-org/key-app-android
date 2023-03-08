package org.p2p.wallet.receive.tokenselect

import kotlinx.coroutines.launch
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.receive.tokenselect.ReceiveTokensMapper.toTokenFinanceCellModel
import org.p2p.wallet.receive.tokenselect.models.ReceiveNetwork
import org.p2p.wallet.receive.tokenselect.models.ReceiveTokenPayload
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.emptyString

private const val PAGE_SIZE = 20

class ReceiveTokensPresenter(
    private val interactor: UserInteractor,
) : BasePresenter<ReceiveTokensContract.View>(), ReceiveTokensContract.Presenter {

    private var searchText = emptyString()
    private var scrollToUp = false

    private var solToken: Token? = null
    private var ethToken: Token? = null

    private var lastSelectedTokenPayload: ReceiveTokenPayload? = null

    override fun attach(view: ReceiveTokensContract.View) {
        super.attach(view)
        launch {
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
        }
    }

    override fun load(isRefresh: Boolean, scrollToUp: Boolean) {
        launch {
            this@ReceiveTokensPresenter.scrollToUp = scrollToUp
            interactor.fetchTokens(searchText, PAGE_SIZE, isRefresh)
        }
    }

    override fun onSearchTokenQueryChanged(newQuery: String) {
        searchText = newQuery
        if (newQuery.isBlank()) {
            view?.resetScrollPosition()
        }
        load(isRefresh = true, scrollToUp = true)
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
                    ReceiveNetwork.ETHEREUM -> openReceiveInEthereum()
                }
            }
        }
    }

    private fun observeTokens() {
        launch {
            interactor.getTokenListFlow().collect { data ->
                val isEmpty = data.result.isEmpty()
                view?.showEmptyState(isEmpty)
                view?.setBannerVisibility(!isEmpty && searchText.isEmpty())
                view?.showTokenItems(
                    data.result.map {
                        it.toTokenFinanceCellModel(
                            solTokenUrl = solToken?.iconUrl.orEmpty(),
                            ethTokenUrl = ethToken?.iconUrl.orEmpty()
                        )
                    },
                    scrollToUp
                )
            }
        }
    }
}
