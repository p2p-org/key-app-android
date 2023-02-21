package org.p2p.wallet.swap.ui.jupiter.tokens

import android.os.Bundle
import android.view.View
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.attachAdapter
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentJupiterSwapTokensBinding
import org.p2p.wallet.swap.ui.jupiter.tokens.adapter.SwapTokensAdapter
import org.p2p.wallet.swap.ui.jupiter.tokens.presenter.SwapTokensChangeToken
import org.p2p.wallet.swap.ui.jupiter.tokens.presenter.SwapTokensMapper
import org.p2p.wallet.swap.ui.jupiter.tokens.presenter.SwapTokensPresenter
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_CHANGE_TOKEN = "ARG_CHANGE_TOKEN"

class SwapTokensFragment :
    BaseMvpFragment<SwapTokensContract.View, SwapTokensContract.Presenter>(R.layout.fragment_jupiter_swap_tokens),
    SwapTokensContract.View {

    companion object {
        fun create(tokenToChange: SwapTokensChangeToken): SwapTokensFragment =
            SwapTokensFragment()
                .withArgs(ARG_CHANGE_TOKEN to tokenToChange)
    }

    private val binding: FragmentJupiterSwapTokensBinding by viewBinding()

    private val tokenToChange: SwapTokensChangeToken by args(ARG_CHANGE_TOKEN)

    override val presenter: SwapTokensContract.Presenter
        get() = SwapTokensPresenter(tokenToChange, SwapTokensMapper())

    private val adapter = SwapTokensAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { popBackStack() }

        binding.recyclerViewTokens.attachAdapter(adapter)
    }

    override fun setTokenItems(items: List<AnyCellItem>) {
        adapter.setTokenItems(items)
    }
}
