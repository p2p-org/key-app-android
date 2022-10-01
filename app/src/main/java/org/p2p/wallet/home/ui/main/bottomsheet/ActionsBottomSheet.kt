package org.p2p.wallet.home.ui.main.bottomsheet

import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.databinding.DialogHomeActionsBinding
import org.p2p.wallet.databinding.ViewActionItemBinding
import org.p2p.wallet.receive.analytics.ReceiveAnalytics
import org.p2p.wallet.send.analytics.SendAnalytics
import org.p2p.wallet.swap.analytics.SwapAnalytics
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.withArgs

private const val EXTRA_REQUEST_KEY = "EXTRA_REQUEST_KEY"
private const val EXTRA_RESULT_KEY = "EXTRA_RESULT_KEY"

class HomeActionsBottomSheet : BottomSheetDialogFragment() {

    companion object {
        fun show(
            fm: FragmentManager,
            requestKey: String,
            resultKey: String
        ) = HomeActionsBottomSheet().withArgs(
            EXTRA_REQUEST_KEY to requestKey,
            EXTRA_RESULT_KEY to resultKey
        ).show(fm, HomeActionsBottomSheet::javaClass.name)
    }

    private val resultKey: String by args(EXTRA_RESULT_KEY)
    private val requestKey: String by args(EXTRA_REQUEST_KEY)

    private val sendAnalytics: SendAnalytics by inject()
    private val swapAnalytics: SwapAnalytics by inject()
    private val receiveAnalytics: ReceiveAnalytics by inject()

    private lateinit var binding: DialogHomeActionsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogHomeActionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
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
            viewActionTrade.apply {
                setResultClickListener(HomeAction.TRADE)
                imageViewAction.setImageResource(R.drawable.action_trade_icon)
                textViewActionTitle.setText(R.string.home_actions_trade_title)
                textViewActionSubtitle.setText(R.string.home_actions_trade_subtitle)
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

    private fun ViewActionItemBinding.setResultClickListener(action: HomeAction) {
        viewActionRoot.setOnClickListener {
            logActionButtonClicked(action)

            setFragmentResult(requestKey, bundleOf(resultKey to action))
            dismissAllowingStateLoss()
        }
    }

    private fun logActionButtonClicked(clickedActionButton: HomeAction) {
        when (clickedActionButton) {
            HomeAction.RECEIVE -> receiveAnalytics.logReceiveActionButtonClicked()
            HomeAction.TRADE -> swapAnalytics.logSwapActionButtonClicked()
            HomeAction.SEND -> sendAnalytics.logSendActionButtonClicked()
        }
    }
}

enum class HomeAction {
    BUY, RECEIVE, TRADE, SEND
}
