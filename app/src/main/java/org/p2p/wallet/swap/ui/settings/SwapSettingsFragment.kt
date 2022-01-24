package org.p2p.wallet.swap.ui.settings

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.widget.SnackBarView
import org.p2p.wallet.databinding.FragmentSwapSettingsBinding
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.attachAdapter
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val EXTRA_TOKENS = "EXTRA_TOKENS"
private const val EXTRA_SELECTED_TOKEN = "EXTRA_SELECTED_TOKEN"

class SwapSettingsFragment :
    BaseMvpFragment<SwapSettingsContract.View, SwapSettingsContract.Presenter>(R.layout.fragment_swap_settings),
    SwapSettingsContract.View {

    companion object {
        fun create(tokens: List<Token.Active>, selectedSymbol: String) =
            SwapSettingsFragment()
                .withArgs(
                    EXTRA_TOKENS to tokens,
                    EXTRA_SELECTED_TOKEN to selectedSymbol
                )
    }

    override val presenter: SwapSettingsContract.Presenter by inject()

    private val tokens: List<Token.Active> by args(EXTRA_TOKENS)
    private val selectedSymbol: String by args(EXTRA_SELECTED_TOKEN)

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
}