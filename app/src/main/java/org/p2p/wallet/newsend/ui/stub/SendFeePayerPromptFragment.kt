package org.p2p.wallet.newsend.ui.stub

import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import kotlinx.coroutines.launch
import org.p2p.core.utils.insets.doOnApplyWindowInsets
import org.p2p.core.utils.insets.systemAndIme
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentSendNoAccountBinding
import org.p2p.wallet.home.ui.new.NewSelectTokenFragment
import org.p2p.wallet.newsend.interactor.SendInteractor
import org.p2p.wallet.newsend.model.SendPromptData
import org.p2p.wallet.root.SystemIconsStyle
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_PROMPT_DATA = "ARG_PROMPT_DATA"
private const val ARG_REQUEST_KEY = "ARG_REQUEST_KEY"
private const val ARG_RESULT_KEY = "ARG_RESULT_KEY"

class SendNoAccountFragment : BaseFragment(R.layout.fragment_send_no_account) {

    companion object {
        fun create(
            promptData: SendPromptData,
            requestKey: String,
            resultKey: String
        ): SendNoAccountFragment = SendNoAccountFragment()
            .withArgs(
                ARG_PROMPT_DATA to promptData,
                ARG_REQUEST_KEY to requestKey,
                ARG_RESULT_KEY to resultKey,
            )
    }

    private val requestKey: String by args(ARG_REQUEST_KEY)
    private val resultKey: String by args(ARG_RESULT_KEY)
    private val promptData: SendPromptData by args(ARG_PROMPT_DATA)

    override val customNavigationBarStyle: SystemIconsStyle
        get() = if (hasAlternativeFeePayerToken()) SystemIconsStyle.WHITE else SystemIconsStyle.BLACK

    private val sendInteractor: SendInteractor by inject()

    private val binding: FragmentSendNoAccountBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            buttonOk.setOnClickListener { popBackStack() }
            buttonContinue.apply {
                text = getString(R.string.send_no_account_non_critical_continue, promptData.feePayerSymbol)
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

    override fun applyWindowInsets(rootView: View) {
        binding.containerBottom.doOnApplyWindowInsets { view, insets, initialPadding ->
            val systemAndIme = insets.systemAndIme()
            if (hasAlternativeFeePayerToken()) {
                binding.root.updatePadding(top = systemAndIme.top)
                binding.containerBottom.updatePadding(bottom = initialPadding.bottom + systemAndIme.bottom)
            } else {
                binding.root.updatePadding(top = systemAndIme.top, bottom = systemAndIme.bottom)
            }
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun hasAlternativeFeePayerToken(): Boolean {
        return promptData.alternativeFeePayerTokens.isNotEmpty()
    }

    private fun FragmentSendNoAccountBinding.setMessage() {
        val messageRes = if (promptData.alternativeFeePayerTokens.isNotEmpty()) {
            R.string.send_no_account_non_critical_message
        } else {
            R.string.send_no_account_critical_message
        }
        val message = getString(messageRes, promptData.approximateFeeUsd)
        textViewMessage.text = message
    }

    private fun showFeePayerSelection() {
        lifecycleScope.launch {
            val currentFeePayerToken = sendInteractor.getFeePayerToken()
            replaceFragment(
                target = NewSelectTokenFragment.create(
                    tokensToSelectFrom = promptData.alternativeFeePayerTokens,
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
