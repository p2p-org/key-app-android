package org.p2p.wallet.history.ui.token

import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.core.token.Token
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.common.feature_toggles.toggles.remote.NewBuyFeatureToggle
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.databinding.FragmentTokenHistoryBinding
import org.p2p.wallet.history.ui.detailsbottomsheet.HistoryTransactionDetailsBottomSheetFragment
import org.p2p.wallet.history.ui.historylist.HistoryListViewContract
import org.p2p.wallet.moonpay.ui.BuySolanaFragment
import org.p2p.wallet.moonpay.ui.new.NewBuyFragment
import org.p2p.wallet.moonpay.ui.transaction.SellTransactionDetailsBottomSheet
import org.p2p.wallet.newsend.ui.search.NewSearchFragment
import org.p2p.wallet.receive.analytics.ReceiveAnalytics
import org.p2p.wallet.receive.token.ReceiveTokenFragment
import org.p2p.wallet.sell.ui.payload.SellPayloadFragment
import org.p2p.wallet.swap.ui.orca.OrcaSwapFragment
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.showErrorDialog
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val EXTRA_TOKEN = "EXTRA_TOKEN"

class TokenHistoryFragment :
    BaseMvpFragment<TokenHistoryContract.View, TokenHistoryContract.Presenter>(R.layout.fragment_token_history),
    TokenHistoryContract.View {

    companion object {
        fun create(tokenForHistory: Token.Active): TokenHistoryFragment =
            TokenHistoryFragment()
                .withArgs(EXTRA_TOKEN to tokenForHistory)
    }

    override val presenter: TokenHistoryContract.Presenter by inject { parametersOf(tokenForHistory) }

    private val tokenForHistory: Token.Active by args(EXTRA_TOKEN)

    private val binding: FragmentTokenHistoryBinding by viewBinding()

    private val receiveAnalytics: ReceiveAnalytics by inject()

    private val newBuyFeatureToggle: NewBuyFeatureToggle by inject()

    private val historyListViewPresenter: HistoryListViewContract.Presenter by inject { parametersOf(tokenForHistory) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupView()
        listenForSellTransactionDialogDismiss()
        lifecycle.addObserver(presenter)
    }

    private fun listenForSellTransactionDialogDismiss() {
        childFragmentManager.setFragmentResultListener(
            SellTransactionDetailsBottomSheet.REQUEST_KEY_DISMISSED, this
        ) { _, _ -> binding.layoutHistoryList.loadHistory() }
    }

    private fun FragmentTokenHistoryBinding.setupView() {
        toolbar.setupToolbar()

        totalTextView.text = tokenForHistory.getFormattedTotal(includeSymbol = true)
        usdTotalTextView.text = tokenForHistory.getFormattedUsdTotal()
        viewActionButtons.onButtonClicked = { onActionButtonClicked(it) }
        binding.layoutHistoryList.apply {
            bind(
                historyListViewPresenter = historyListViewPresenter,
                onBuyClicked = { onActionButtonClicked(ActionButton.BUY_BUTTON) },
                onReceiveClicked = { onActionButtonClicked(ActionButton.RECEIVE_BUTTON) },
                onTransactionClicked = presenter::onTransactionClicked,
                onSellTransactionClicked = presenter::onSellTransactionClicked,
                token = tokenForHistory
            )
            addObserver(lifecycle)
        }
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
                replaceFragment(ReceiveTokenFragment.create(tokenForHistory))
            }
            ActionButton.SEND_BUTTON -> {
                replaceFragment(NewSearchFragment.create(tokenForHistory))
            }
            ActionButton.SWAP_BUTTON -> {
                replaceFragment(OrcaSwapFragment.create(tokenForHistory))
            }
            ActionButton.SELL_BUTTON -> {
                replaceFragment(SellPayloadFragment.create())
            }
        }
    }

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
}
