package org.p2p.wallet.swap.ui.settings

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.analytics.AnalyticsInteractor
import org.p2p.wallet.common.analytics.ScreenName
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.common.ui.widget.SnackBarView
import org.p2p.wallet.databinding.FragmentSwapSettingsBinding
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.swap.analytics.SwapAnalytics
import org.p2p.wallet.swap.model.Slippage
import org.p2p.wallet.swap.model.orca.OrcaSettingsResult
import org.p2p.wallet.swap.ui.orca.KEY_REQUEST_SWAP
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.attachAdapter
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val EXTRA_SLIPPAGE = "EXTRA_SLIPPAGE"
private const val EXTRA_TOKENS = "EXTRA_TOKENS"
private const val EXTRA_SELECTED_TOKEN = "EXTRA_SELECTED_TOKEN"
private const val EXTRA_RESULT_KEY = "EXTRA_RESULT_KEY"

class SwapSettingsFragment : BaseFragment(R.layout.fragment_swap_settings) {

    companion object {
        fun create(
            currentSlippage: Slippage,
            tokens: List<Token.Active>,
            currentFeePayerToken: Token.Active,
            resultKey: String
        ) =
            SwapSettingsFragment()
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
    private val analyticsInteractor: AnalyticsInteractor by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        analyticsInteractor.logScreenOpenEvent(ScreenName.Swap.SETTINGS)
        with(binding) {
            toolbar.setNavigationOnClickListener {
                updateSettings()
                popBackStack()
            }
            tokensRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            tokensRecyclerView.attachAdapter(tokensAdapter)
            tokensAdapter.setItems(tokens)

            minRadioButton.text = Slippage.MIN.percentValue
            mediumRadioButton.text = Slippage.MEDIUM.percentValue
            percentRadioButton.text = Slippage.PERCENT.percentValue
            fiveRadioButton.text = Slippage.FIVE.percentValue

            checkSlippage()

            slippageRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                currentSlippage = when (checkedId) {
                    R.id.minRadioButton -> Slippage.MIN
                    R.id.mediumRadioButton -> Slippage.MEDIUM
                    R.id.percentRadioButton -> Slippage.PERCENT
                    R.id.fiveRadioButton -> Slippage.FIVE
                    else -> currentSlippage
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            updateSettings()
            popBackStack()
        }
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
            Slippage.MIN -> binding.slippageRadioGroup.check(R.id.minRadioButton)
            Slippage.MEDIUM -> binding.slippageRadioGroup.check(R.id.mediumRadioButton)
            Slippage.PERCENT -> binding.slippageRadioGroup.check(R.id.percentRadioButton)
            Slippage.FIVE -> binding.slippageRadioGroup.check(R.id.fiveRadioButton)
            else -> binding.slippageRadioGroup.check(R.id.percentRadioButton)
        }
    }

    private fun showMessage(tokenSymbol: String) {
        val message = getString(R.string.swap_pay_fee_format, tokenSymbol)
        SnackBarView.make(
            requireView(),
            message,
            R.drawable.ic_done
        )?.show()
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
        val result = OrcaSettingsResult(currentSlippage, selectedToken)
        setFragmentResult(KEY_REQUEST_SWAP, bundleOf(resultKey to result))
    }
}