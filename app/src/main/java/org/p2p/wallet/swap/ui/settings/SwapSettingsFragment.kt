package org.p2p.wallet.swap.ui.settings

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.widget.SnackBarView
import org.p2p.wallet.databinding.FragmentSwapSettingsBinding
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.swap.model.Slippage
import org.p2p.wallet.swap.ui.orca.KEY_REQUEST_SWAP
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.attachAdapter
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val EXTRA_TOKENS = "EXTRA_TOKENS"
private const val EXTRA_SELECTED_TOKEN = "EXTRA_SELECTED_TOKEN"
private const val EXTRA_RESULT_KEY = "EXTRA_RESULT_KEY"

class SwapSettingsFragment :
    BaseMvpFragment<SwapSettingsContract.View, SwapSettingsContract.Presenter>(R.layout.fragment_swap_settings),
    SwapSettingsContract.View {

    companion object {
        fun create(tokens: List<Token.Active>, selectedSymbol: String, resultKey: String) =
            SwapSettingsFragment()
                .withArgs(
                    EXTRA_TOKENS to tokens,
                    EXTRA_SELECTED_TOKEN to selectedSymbol,
                    EXTRA_RESULT_KEY to resultKey
                )
    }

    override val presenter: SwapSettingsContract.Presenter by inject()

    private val tokens: List<Token.Active> by args(EXTRA_TOKENS)
    private val selectedSymbol: String by args(EXTRA_SELECTED_TOKEN)
    private val resultKey: String by args(EXTRA_RESULT_KEY)

    private val tokensAdapter: SwapSettingsTokensAdapter by lazy {
        SwapSettingsTokensAdapter(selectedSymbol) {
            presenter.setFeePayToken(it)
            showMessage(it.tokenSymbol)
        }
    }

    private val binding: FragmentSwapSettingsBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            tokensRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            tokensRecyclerView.attachAdapter(tokensAdapter)
            tokensAdapter.setItems(tokens)

            minRadioButton.text = Slippage.MIN.percentValue
            mediumRadioButton.text = Slippage.MEDIUM.percentValue
            percentRadioButton.text = Slippage.PERCENT.percentValue
            fiveRadioButton.text = Slippage.FIVE.percentValue

            slippageRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.minRadioButton -> setSlippage(Slippage.MIN)
                    R.id.mediumRadioButton -> setSlippage(Slippage.MEDIUM)
                    R.id.percentRadioButton -> setSlippage(Slippage.PERCENT)
                    R.id.fiveRadioButton -> setSlippage(Slippage.FIVE)
                }
            }
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

    private fun setSlippage(slippage: Slippage) {
        setFragmentResult(KEY_REQUEST_SWAP, bundleOf(resultKey to slippage))
    }
}