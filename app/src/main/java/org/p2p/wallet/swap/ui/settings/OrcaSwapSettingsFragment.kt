package org.p2p.wallet.swap.ui.settings

import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextWatcher
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.core.token.Token
import org.p2p.uikit.utils.attachAdapter
import org.p2p.uikit.utils.focusAndShowKeyboard
import org.p2p.wallet.R
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentSwapSettingsBinding
import org.p2p.wallet.swap.analytics.SwapAnalytics
import org.p2p.wallet.swap.model.Slippage
import org.p2p.wallet.swap.model.Slippage.Companion.MAX_ALLOWED_SLIPPAGE
import org.p2p.wallet.swap.model.Slippage.Companion.PERCENT_DIVIDE_VALUE
import org.p2p.wallet.swap.model.orca.OrcaSettingsResult
import org.p2p.wallet.swap.ui.orca.KEY_REQUEST_SWAP
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.snackbar
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val EXTRA_SLIPPAGE = "EXTRA_SLIPPAGE"
private const val EXTRA_TOKENS = "EXTRA_TOKENS"
private const val EXTRA_SELECTED_TOKEN = "EXTRA_SELECTED_TOKEN"
private const val EXTRA_RESULT_KEY = "EXTRA_RESULT_KEY"

class OrcaSwapSettingsFragment : BaseFragment(R.layout.fragment_swap_settings) {

    companion object {
        fun create(
            currentSlippage: Slippage,
            tokens: List<Token.Active>,
            currentFeePayerToken: Token.Active,
            resultKey: String
        ) =
            OrcaSwapSettingsFragment()
                .withArgs(
                    EXTRA_SLIPPAGE to currentSlippage,
                    EXTRA_TOKENS to tokens,
                    EXTRA_SELECTED_TOKEN to currentFeePayerToken,
                    EXTRA_RESULT_KEY to resultKey
                )
    }

    private var currentSlippage: Slippage by args(EXTRA_SLIPPAGE)
    private val tokens: List<Token.Active> by args(EXTRA_TOKENS)
    private var selectedToken: Token.Active by args(EXTRA_SELECTED_TOKEN)
    private val resultKey: String by args(EXTRA_RESULT_KEY)
    private val swapAnalytics: SwapAnalytics by inject()
    private val tokensAdapter: SwapSettingsTokensAdapter by lazy {
        SwapSettingsTokensAdapter(selectedToken) { onTokenSelected(it) }
    }

    private val binding: FragmentSwapSettingsBinding by viewBinding()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()
    private lateinit var validateWatcher: TextWatcher

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        analyticsInteractor.logScreenOpenEvent(ScreenNames.Swap.SETTINGS)
        with(binding) {
            toolbar.setNavigationOnClickListener {
                updateSettings()
                popBackStack()
            }
            tokensRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            tokensRecyclerView.attachAdapter(tokensAdapter)
            tokensAdapter.setItems(tokens)

            minRadioButton.text = Slippage.Min.percentValue
            mediumRadioButton.text = Slippage.Medium.percentValue
            percentRadioButton.text = Slippage.TopUpSlippage.percentValue
            fiveRadioButton.text = Slippage.Five.percentValue

            checkSlippage()

            validateWatcher = slippageInputTextView.doAfterTextChanged {
                val value = (it.toString().toDoubleOrNull() ?: 0.0)
                when {
                    value > MAX_ALLOWED_SLIPPAGE -> {
                        slippageInputTextViewLayout.error = getString(R.string.settings_slippage_max_error)
                        binding.slippageInputTextView.apply {
                            removeTextChangedListener(validateWatcher)
                            setText(MAX_ALLOWED_SLIPPAGE.toString())
                            setSelection(MAX_ALLOWED_SLIPPAGE.toString().length)
                            addTextChangedListener(validateWatcher)
                        }
                    }
                    value < Slippage.Min.doubleValue ->
                        slippageInputTextViewLayout.error = getString(R.string.settings_slippage_min_error)
                    else ->
                        slippageInputTextViewLayout.error = null
                }
            }

            slippageRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                val isCustomSlippage = checkedId == R.id.customRadioButton
                slippageInputTextViewLayout.isVisible = isCustomSlippage
                if (isCustomSlippage) {
                    slippageInputTextView.focusAndShowKeyboard()
                } else {
                    currentSlippage = when (checkedId) {
                        R.id.minRadioButton -> Slippage.Min
                        R.id.mediumRadioButton -> Slippage.Medium
                        R.id.percentRadioButton -> Slippage.TopUpSlippage
                        R.id.fiveRadioButton -> Slippage.Five
                        else -> currentSlippage
                    }
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            updateSettings()
            popBackStack()
        }
    }

    override fun onDestroyView() {
        binding.slippageInputTextView.removeTextChangedListener(validateWatcher)
        super.onDestroyView()
    }

    override fun onDetach() {
        super.onDetach()
        val feeSource = if (selectedToken.isSOL) SwapAnalytics.FeeSource.SOL else SwapAnalytics.FeeSource.OTHER
        swapAnalytics.logSwapSettingSettings(
            priceSlippage = currentSlippage.doubleValue,
            priceSlippageExact = false,
            feesSource = feeSource
        )
    }

    private fun checkSlippage() {
        when (currentSlippage) {
            Slippage.Min -> binding.slippageRadioGroup.check(R.id.minRadioButton)
            Slippage.Medium -> binding.slippageRadioGroup.check(R.id.mediumRadioButton)
            Slippage.TopUpSlippage -> binding.slippageRadioGroup.check(R.id.percentRadioButton)
            Slippage.Five -> binding.slippageRadioGroup.check(R.id.fiveRadioButton)
            else -> with(binding) {
                slippageRadioGroup.check(R.id.customRadioButton)
                slippageInputTextView.setText(currentSlippage.percentValue)
                slippageInputTextViewLayout.isVisible = true
            }
        }
    }

    private fun showMessage(tokenSymbol: String) {
        val message = getString(R.string.swap_pay_fee_format, tokenSymbol)
        snackbar { it.setMessage(message) }
    }

    /* There are couple of elements in the list and it's okay for us to update all list */
    @SuppressLint("NotifyDataSetChanged")
    private fun onTokenSelected(token: Token.Active) {
        binding.tokensRecyclerView.post {
            selectedToken = token
            tokensAdapter.notifyDataSetChanged()
            updateSettings()
            showMessage(token.tokenSymbol)
        }
    }

    private fun updateSettings() {
        if (binding.slippageRadioGroup.checkedRadioButtonId == R.id.customRadioButton) {
            val slippageValue = binding.slippageInputTextView.text.toString().toDoubleOrNull() ?: 0.0
            currentSlippage = Slippage.parse(slippageValue / PERCENT_DIVIDE_VALUE)
        }
        val result = OrcaSettingsResult(currentSlippage, selectedToken)
        setFragmentResult(KEY_REQUEST_SWAP, bundleOf(resultKey to result))
    }
}
