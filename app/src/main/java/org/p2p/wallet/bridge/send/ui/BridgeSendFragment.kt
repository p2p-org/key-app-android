package org.p2p.wallet.bridge.send.ui

import androidx.core.view.isVisible
import android.content.Context
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import java.math.BigDecimal
import org.p2p.core.token.Token
import org.p2p.uikit.components.UiKitSendDetailsWidgetContract
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSendNewBinding
import org.p2p.wallet.newsend.model.SearchResult
import org.p2p.wallet.newsend.ui.SendOpenedFrom
import org.p2p.wallet.root.RootListener
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_RECIPIENT = "ARG_RECIPIENT"
private const val ARG_INITIAL_TOKEN = "ARG_INITIAL_TOKEN"
private const val ARG_INPUT_AMOUNT = "ARG_INPUT_AMOUNT"
private const val ARG_OPENED_FROM = "ARG_OPENED_FROM"

private const val KEY_RESULT_FEE = "KEY_RESULT_FEE"
private const val KEY_RESULT_FEE_PAYER_TOKENS = "KEY_RESULT_FEE_PAYER_TOKENS"
private const val KEY_RESULT_NEW_FEE_PAYER = "KEY_RESULT_APPROXIMATE_FEE_USD"
private const val KEY_RESULT_TOKEN_TO_SEND = "KEY_RESULT_TOKEN_TO_SEND"
private const val KEY_REQUEST_SEND = "KEY_REQUEST_SEND"

class BridgeSendFragment :
    BaseMvpFragment<BridgeSendContract.View, BridgeSendContract.Presenter>(R.layout.fragment_send_new),
    BridgeSendContract.View {

    companion object {
        fun create(
            recipient: SearchResult,
            initialToken: Token.Active? = null,
            inputAmount: BigDecimal? = null,
            openedFrom: SendOpenedFrom = SendOpenedFrom.MAIN_FLOW,
        ): BridgeSendFragment = BridgeSendFragment()
            .withArgs(
                ARG_RECIPIENT to recipient,
                ARG_INITIAL_TOKEN to initialToken,
                ARG_INPUT_AMOUNT to inputAmount,
                ARG_OPENED_FROM to openedFrom
            )
    }

    private val recipient: SearchResult by args(ARG_RECIPIENT)
    private val initialToken: Token.Active? by args(ARG_INITIAL_TOKEN)
    private val inputAmount: BigDecimal? by args(ARG_INPUT_AMOUNT)
    private val openedFrom: SendOpenedFrom by args(ARG_OPENED_FROM)
    private val widgetDelegate: UiKitSendDetailsWidgetContract by lazy {
        BridgeSendWidgetDelegate(
            widget = binding.widgetSendDetails,
            slider = binding.sliderSend,
            presenter = presenter
        )
    }

    private val binding: FragmentSendNewBinding by viewBinding()

    override val presenter: BridgeSendContract.Presenter by inject { parametersOf(recipient) }

    private var listener: RootListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? RootListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.textViewDebug.isVisible = BuildConfig.DEBUG
        presenter.attach(widgetDelegate)
    }
}
