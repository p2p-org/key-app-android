package org.p2p.wallet.newsend.noaccount

import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentSendNoAccountBinding
import org.p2p.wallet.home.ui.new.NewSelectTokenFragment
import org.p2p.wallet.send.interactor.SendInteractor
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.addFragment
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import kotlinx.coroutines.launch

private const val ARG_TOKEN_SYMBOL = "ARG_TOKEN_SYMBOL"
private const val ARG_HAS_ALTERNATIVE_TOKEN = "ARG_HAS_ALTERNATIVE_TOKEN"
private const val ARG_APPROXIMATE_FEE = "ARG_APPROXIMATE_FEE"
private const val ARG_REQUEST_KEY = "ARG_REQUEST_KEY"
private const val ARG_RESULT_KEY = "ARG_RESULT_KEY"

class SendNoAccountFragment : BaseFragment(R.layout.fragment_send_no_account) {

    companion object {
        fun create(
            tokenSymbol: String,
            approximateFeeUsd: String,
            hasAlternativeFeePayerToken: Boolean,
            requestKey: String,
            resultKey: String
        ): SendNoAccountFragment = SendNoAccountFragment()
            .withArgs(
                ARG_TOKEN_SYMBOL to tokenSymbol,
                ARG_APPROXIMATE_FEE to approximateFeeUsd,
                ARG_HAS_ALTERNATIVE_TOKEN to hasAlternativeFeePayerToken,
                ARG_REQUEST_KEY to requestKey,
                ARG_RESULT_KEY to resultKey,
            )
    }

    private val requestKey: String by args(ARG_REQUEST_KEY)
    private val resultKey: String by args(ARG_RESULT_KEY)
    private val tokenSymbol: String by args(ARG_TOKEN_SYMBOL)
    private val approximateFeeUsd: String by args(ARG_APPROXIMATE_FEE)
    private val hasAlternativeFeePayerToken: Boolean by args(ARG_HAS_ALTERNATIVE_TOKEN)

    override val statusBarColor: Int get() = R.color.bg_smoke
    override val navBarColor: Int get() = R.color.bg_night

    private val userInteractor: UserInteractor by inject()
    private val sendInteractor: SendInteractor by inject()

    private val binding: FragmentSendNoAccountBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            buttonOk.setOnClickListener { popBackStack() }
            buttonContinue.apply {
                text = getString(R.string.send_no_account_non_critical_continue, tokenSymbol)
                setOnClickListener { popBackStack() }
            }
            buttonSwitch.setOnClickListener {
                showFeePayerSelection()
            }
            textViewTitle.setText(R.string.send_no_account_title)

            containerBottom.isVisible = hasAlternativeFeePayerToken
            buttonOk.isVisible = !hasAlternativeFeePayerToken

            setMessage()
        }
    }

    private fun FragmentSendNoAccountBinding.setMessage() {
        val messageRes = if (hasAlternativeFeePayerToken) {
            R.string.send_no_account_non_critical_message
        } else {
            R.string.send_no_account_critical_message
        }
        val message = getString(messageRes, approximateFeeUsd)
        textViewMessage.text = message
    }

    private fun showFeePayerSelection() {
        lifecycleScope.launch {
            val tokens = userInteractor.getUserTokens()
            val currentFeePayerToken = sendInteractor.getFeePayerToken()
            addFragment(
                target = NewSelectTokenFragment.create(
                    tokens = tokens,
                    selectedToken = currentFeePayerToken,
                    requestKey = requestKey,
                    resultKey = resultKey,
                    title = getString(R.string.send_pick_fee_token_format, approximateFeeUsd)
                ),
                enter = R.anim.slide_up,
                exit = 0,
                popExit = R.anim.slide_down,
                popEnter = 0
            )
        }
    }
}
