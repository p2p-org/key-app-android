package org.p2p.wallet.history.ui.token

import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import android.content.Context
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.common.feature_toggles.toggles.remote.NewBuyFeatureToggle
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.databinding.FragmentTokenHistoryBinding
import org.p2p.wallet.history.ui.detailsbottomsheet.HistoryTransactionDetailsBottomSheetFragment
import org.p2p.wallet.history.ui.historylist.HistoryListViewClickListener
import org.p2p.wallet.history.ui.historylist.HistoryListViewContract
import org.p2p.wallet.history.ui.historylist.HistoryListViewType
import org.p2p.wallet.jupiter.model.SwapOpenedFrom
import org.p2p.wallet.jupiter.ui.main.JupiterSwapFragment
import org.p2p.wallet.moonpay.ui.BuySolanaFragment
import org.p2p.wallet.moonpay.ui.new.NewBuyFragment
import org.p2p.wallet.moonpay.ui.transaction.SellTransactionDetailsBottomSheet
import org.p2p.wallet.newsend.model.SearchOpenedFromScreen
import org.p2p.wallet.newsend.ui.search.NewSearchFragment
import org.p2p.wallet.receive.analytics.ReceiveAnalytics
import org.p2p.wallet.receive.eth.EthereumReceiveFragment
import org.p2p.wallet.receive.solana.NewReceiveSolanaFragment
import org.p2p.wallet.receive.solana.ReceiveSolanaFragment
import org.p2p.wallet.receive.tokenselect.dialog.SelectReceiveNetworkBottomSheet
import org.p2p.wallet.receive.tokenselect.models.ReceiveNetwork
import org.p2p.wallet.sell.ui.payload.SellPayloadFragment
import org.p2p.wallet.root.RootListener
import org.p2p.wallet.transaction.model.NewShowProgress
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.getSerializableOrNull
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.showErrorDialog
import org.p2p.wallet.utils.toBase58Instance
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val EXTRA_TOKEN = "EXTRA_TOKEN"

private const val KEY_REQUEST_NETWORK = "KEY_REQUEST_NETWORK"
private const val KEY_RESULT_NETWORK = "KEY_RESULT_NETWORK"

class TokenHistoryFragment :
    BaseMvpFragment<TokenHistoryContract.View, TokenHistoryContract.Presenter>(R.layout.fragment_token_history),
    TokenHistoryContract.View,
    HistoryListViewClickListener {

    companion object {
        fun create(tokenForHistory: Token.Active): TokenHistoryFragment =
            TokenHistoryFragment()
                .withArgs(EXTRA_TOKEN to tokenForHistory)
    }

    override val presenter: TokenHistoryContract.Presenter by inject { parametersOf(tokenForHistory) }
    private val historyListPresenter: HistoryListViewContract.Presenter by inject()

    private val tokenForHistory: Token.Active by args(EXTRA_TOKEN)

    private val binding: FragmentTokenHistoryBinding by viewBinding()

    private val receiveAnalytics: ReceiveAnalytics by inject()

    private val newBuyFeatureToggle: NewBuyFeatureToggle by inject()
    private var listener: RootListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? RootListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupView()
        listenForSellTransactionDialogDismiss()
    }

    private fun listenForSellTransactionDialogDismiss() {
        childFragmentManager.setFragmentResultListener(
            SellTransactionDetailsBottomSheet.REQUEST_KEY_DISMISSED, this
        ) { _, _ -> binding.layoutHistoryList.loadHistory() }
    }

    private fun FragmentTokenHistoryBinding.setupView() {
        toolbar.setupToolbar()

        viewActionButtons.onButtonClicked = ::onActionButtonClicked
        binding.layoutHistoryList.bind(
            presenter = historyListPresenter,
            clickListener = this@TokenHistoryFragment,
            listType = HistoryListViewType.HistoryForToken(tokenForHistory.mintAddress.toBase58Instance())
        )
        childFragmentManager.setFragmentResultListener(
            KEY_REQUEST_NETWORK,
            viewLifecycleOwner,
            ::onFragmentResult
        )
    }

    private fun Toolbar.setupToolbar() {
        title = tokenForHistory.tokenName

        setNavigationOnClickListener { popBackStack() }
        if (BuildConfig.DEBUG) {
            inflateMenu(R.menu.menu_history)
            setOnMenuItemClickListener {
                var isHandled = false
                if (it.itemId == R.id.closeItem) {
                    presenter.closeAccount()
                    isHandled = true
                }
                isHandled
            }
        }
    }

    override fun renderTokenAmounts(token: Token.Active) {
        binding.totalTextView.text = token.getFormattedTotal(includeSymbol = true)
        binding.usdTotalTextView.text = token.getFormattedUsdTotal()
    }

    override fun loadTokenHistoryList() {
        binding.layoutHistoryList.loadHistory()
    }

    private fun onFragmentResult(requestKey: String, result: Bundle) {
        result.getSerializableOrNull<ReceiveNetwork>(KEY_RESULT_NETWORK)?.let { network ->
            receiveAnalytics.logNetworkClicked(network)
            when (network) {
                ReceiveNetwork.SOLANA -> openReceiveInSolana()
                ReceiveNetwork.ETHEREUM -> openReceiveInEthereum()
            }
        }
    }

    private fun onActionButtonClicked(clickedButton: ActionButton) {
        when (clickedButton) {
            ActionButton.BUY_BUTTON -> {
                replaceFragment(
                    if (newBuyFeatureToggle.value) {
                        NewBuyFragment.create(tokenForHistory)
                    } else {
                        BuySolanaFragment.create(tokenForHistory)
                    }
                )
            }
            ActionButton.RECEIVE_BUTTON -> {
                receiveAnalytics.logTokenReceiveViewed(tokenForHistory.tokenName)
                presenter.onReceiveClicked()
            }
            ActionButton.SEND_BUTTON -> {
                replaceFragment(NewSearchFragment.create(tokenForHistory, SearchOpenedFromScreen.MAIN))
            }
            ActionButton.SWAP_BUTTON -> {
                replaceFragment(JupiterSwapFragment.create(tokenForHistory, SwapOpenedFrom.TOKEN_SCREEN))
            }
            ActionButton.SELL_BUTTON -> {
                replaceFragment(SellPayloadFragment.create())
            }
        }
    }

    override fun onTransactionClicked(transactionId: String) {
        presenter.onTransactionClicked(transactionId)
    }

    override fun onSellTransactionClicked(transactionId: String) {
        presenter.onSellTransactionClicked(transactionId)
    }

    override fun onSwapBannerClicked(
        sourceTokenMint: String,
        destinationTokenMint: String,
        sourceSymbol: String,
        destinationSymbol: String,
        openedFrom: SwapOpenedFrom
    ) {
        replaceFragment(
            JupiterSwapFragment.create(
                tokenASymbol = sourceSymbol,
                tokenBSymbol = destinationSymbol,
                amountA = Constants.ZERO_AMOUNT,
                source = openedFrom
            )
        )
    }

    override fun onBridgeSendClicked(transactionId: String) {
        presenter.onBridgePendingSendClicked(transactionId)
    }

    override fun onBridgeClaimClicked(transactionId: String) {
        presenter.onBridgePendingClaimClicked(transactionId)
    }

    override fun onUserSendLinksClicked() = Unit

    override fun showError(@StringRes resId: Int, argument: String) {
        showErrorDialog(getString(resId, argument))
    }

    override fun showActionButtons(actionButtons: List<ActionButton>) {
        binding.viewActionButtons.showActionButtons(actionButtons)
    }

    override fun showDetailsScreen(transactionId: String) {
        HistoryTransactionDetailsBottomSheetFragment.show(
            fragmentManager = parentFragmentManager,
            transactionId = transactionId
        )
    }

    override fun openSellTransactionDetails(transactionId: String) {
        SellTransactionDetailsBottomSheet.show(childFragmentManager, transactionId)
    }

    override fun openOldReceiveInSolana() {
        replaceFragment(ReceiveSolanaFragment.create(token = tokenForHistory))
    }

    override fun showReceiveNetworkDialog() {
        receiveAnalytics.logNetworkSelectionScreenOpened()
        SelectReceiveNetworkBottomSheet.show(
            fm = childFragmentManager,
            title = getString(R.string.receive_network_dialog_title),
            requestKey = KEY_REQUEST_NETWORK,
            resultKey = KEY_RESULT_NETWORK
        )
    }

    override fun openReceiveInSolana() = with(tokenForHistory) {
        replaceFragment(
            NewReceiveSolanaFragment.create(
                tokenLogoUrl = iconUrl.orEmpty(),
                tokenSymbol = tokenSymbol
            )
        )
    }

    override fun openReceiveInEthereum() = with(tokenForHistory) {
        replaceFragment(
            EthereumReceiveFragment.create(
                tokenLogoUrl = iconUrl.orEmpty(),
                tokenSymbol = tokenSymbol
            )
        )
    }

    override fun showProgressDialog(bundleId: String, progressDetails: NewShowProgress) {
        listener?.showTransactionProgress(bundleId, progressDetails)
    }
}
