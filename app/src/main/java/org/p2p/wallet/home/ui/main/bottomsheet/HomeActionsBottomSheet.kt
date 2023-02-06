package org.p2p.wallet.home.ui.main.bottomsheet

import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.viewbinding.ViewBinding
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpBottomSheet
import org.p2p.wallet.databinding.DialogHomeActionsBinding
import org.p2p.wallet.newsend.analytics.NewSendAnalytics
import org.p2p.wallet.receive.analytics.ReceiveAnalytics
import org.p2p.wallet.sell.analytics.SellAnalytics
import org.p2p.wallet.swap.analytics.SwapAnalytics
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val EXTRA_REQUEST_KEY = "EXTRA_REQUEST_KEY"
private const val EXTRA_RESULT_KEY = "EXTRA_RESULT_KEY"

class HomeActionsBottomSheet :
    BaseMvpBottomSheet<HomeActionsContract.View, HomeActionsContract.Presenter>(R.layout.dialog_home_actions),
    HomeActionsContract.View {

    companion object {
        fun show(
            fm: FragmentManager,
            requestKey: String,
            resultKey: String
        ) {
            HomeActionsBottomSheet().withArgs(
                EXTRA_REQUEST_KEY to requestKey,
                EXTRA_RESULT_KEY to resultKey
            )
                .show(fm, HomeActionsBottomSheet::javaClass.name)
        }
    }

    private val resultKey: String by args(EXTRA_RESULT_KEY)
    private val requestKey: String by args(EXTRA_REQUEST_KEY)

    private val sendAnalytics: NewSendAnalytics by inject()
    private val swapAnalytics: SwapAnalytics by inject()
    private val receiveAnalytics: ReceiveAnalytics by inject()
    private val sellAnalytics: SellAnalytics by inject()

    override val presenter: HomeActionsContract.Presenter by inject()

    private val binding: DialogHomeActionsBinding by viewBinding()

    override fun onStart() {
        super.onStart()
        expandToFitAllContent()
    }

    override fun setupHomeActions(isSellFeatureEnabled: Boolean) {
        with(binding) {
            if (isSellFeatureEnabled) {
                viewActionSell.apply {
                    root.isVisible = true

                    setResultClickListener(HomeAction.SELL)
                    imageViewAction.setImageResource(R.drawable.action_sell_icon)
                    textViewActionTitle.setText(R.string.home_actions_sell_title)
                    textViewActionSubtitle.setText(R.string.home_actions_sell_subtitle)
                }
            }
            viewActionBuy.apply {
                setResultClickListener(HomeAction.BUY)
                imageViewAction.setImageResource(R.drawable.action_buy_icon)
                textViewActionTitle.setText(R.string.home_actions_buy_title)
                textViewActionSubtitle.setText(R.string.home_actions_buy_subtitle)
            }
            viewActionReceive.apply {
                setResultClickListener(HomeAction.RECEIVE)
                imageViewAction.setImageResource(R.drawable.action_receive_icon)
                textViewActionTitle.setText(R.string.home_actions_receive_title)
                textViewActionSubtitle.setText(R.string.home_actions_receive_subtitle)
            }
            viewActionSwap.apply {
                setResultClickListener(HomeAction.SWAP)
                imageViewAction.setImageResource(R.drawable.action_swap_icon)
                textViewActionTitle.setText(R.string.home_actions_swap_title)
                textViewActionSubtitle.setText(R.string.home_actions_swap_subtitle)
            }
            viewActionSend.apply {
                setResultClickListener(HomeAction.SEND)
                imageViewAction.setImageResource(R.drawable.action_send_icon)
                textViewActionTitle.setText(R.string.home_actions_send_title)
                textViewActionSubtitle.setText(R.string.home_actions_send_subtitle)
            }
        }
    }

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_Rounded

    private fun ViewBinding.setResultClickListener(action: HomeAction) {
        root.setOnClickListener {
            logActionButtonClicked(action)

            setFragmentResult(requestKey, bundleOf(resultKey to action))
            dismissAllowingStateLoss()
        }
    }

    private fun logActionButtonClicked(clickedActionButton: HomeAction) {
        when (clickedActionButton) {
            HomeAction.RECEIVE -> receiveAnalytics.logReceiveActionButtonClicked()
            HomeAction.SWAP -> swapAnalytics.logSwapActionButtonClicked()
            HomeAction.SEND -> sendAnalytics.logSendActionButtonClicked()
            HomeAction.SELL -> sellAnalytics.logCashOutClicked(SellAnalytics.AnalyticsCashOutSource.ACTION_BUTTON)
            else -> Unit
        }
    }
}

enum class HomeAction {
    BUY, RECEIVE, SWAP, SEND, SELL
}
