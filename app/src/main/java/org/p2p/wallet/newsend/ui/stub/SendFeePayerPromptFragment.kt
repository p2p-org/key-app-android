package org.p2p.wallet.newsend.ui.stub

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.p2p.core.token.Token
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentSendNoAccountBinding
import org.p2p.wallet.home.ui.new.NewSelectTokenFragment
import org.p2p.wallet.send.interactor.SendInteractor
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_TOKEN_SYMBOL = "ARG_TOKEN_SYMBOL"
private const val ARG_ALTERNATIVE_TOKENS = "ARG_HAS_ALTERNATIVE_TOKEN"
private const val ARG_APPROXIMATE_FEE = "ARG_APPROXIMATE_FEE"
private const val ARG_REQUEST_KEY = "ARG_REQUEST_KEY"
private const val ARG_RESULT_KEY = "ARG_RESULT_KEY"

class SendNoAccountFragment : BaseFragment(R.layout.fragment_send_no_account) {

    companion object {
        fun create(
            tokenSymbol: String,
            approximateFeeUsd: String,
            alternativeFeePayerTokens: List<Token.Active>,
            requestKey: String,
            resultKey: String
        ): SendNoAccountFragment = SendNoAccountFragment()
            .withArgs(
                ARG_TOKEN_SYMBOL to tokenSymbol,
                ARG_APPROXIMATE_FEE to approximateFeeUsd,
                ARG_ALTERNATIVE_TOKENS to alternativeFeePayerTokens,
                ARG_REQUEST_KEY to requestKey,
                ARG_RESULT_KEY to resultKey,
            )
    }

    private val requestKey: String by args(ARG_REQUEST_KEY)
    private val resultKey: String by args(ARG_RESULT_KEY)
    private val tokenSymbol: String by args(ARG_TOKEN_SYMBOL)
    private val approximateFeeUsd: String by args(ARG_APPROXIMATE_FEE)
    private val alternativeFeePayerTokens: List<Token.Active> by args(ARG_ALTERNATIVE_TOKENS)

    override val statusBarColor: Int get() = R.color.bg_smoke
    override val navBarColor: Int get() = if (hasAlternativeFeePayerToken()) R.color.bg_night else R.color.bg_smoke

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

            val hasAlternativeFeePayerToken = hasAlternativeFeePayerToken()
            containerBottom.isVisible = hasAlternativeFeePayerToken
            buttonOk.isVisible = !hasAlternativeFeePayerToken

            setMessage()
        }
    }

    private fun hasAlternativeFeePayerToken(): Boolean = alternativeFeePayerTokens.isNotEmpty()

    private fun FragmentSendNoAccountBinding.setMessage() {
        val messageRes = if (alternativeFeePayerTokens.isNotEmpty()) {
            R.string.send_no_account_non_critical_message
        } else {
            R.string.send_no_account_critical_message
        }
        val message = getString(messageRes, approximateFeeUsd)
        textViewMessage.text = message
    }

    private fun showFeePayerSelection() {
        lifecycleScope.launch {
            val currentFeePayerToken = sendInteractor.getFeePayerToken()
            replaceFragment(
                target = NewSelectTokenFragment.create(
                    tokens = alternativeFeePayerTokens,
                    selectedToken = currentFeePayerToken,
                    requestKey = requestKey,
                    resultKey = resultKey,
                    title = getString(R.string.send_pick_fee_token_title)
                ),
                enter = R.anim.slide_up,
                exit = 0,
                popExit = R.anim.slide_down,
                popEnter = 0
            )
        }
    }
}
