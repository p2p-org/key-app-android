package org.p2p.wallet.swap.ui.jupiter.tokens

import androidx.core.net.toUri
import android.os.Bundle
import android.view.View
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentJupiterSwapTokensBinding
import org.p2p.wallet.swap.ui.jupiter.tokens.adapter.SwapTokenItem
import org.p2p.wallet.swap.ui.jupiter.tokens.adapter.SwapTokensAdapter
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding

class SwapTokensFragment : BaseFragment(R.layout.fragment_jupiter_swap_tokens) {

    private val binding: FragmentJupiterSwapTokensBinding by viewBinding()

    private val adapter = SwapTokensAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setSearchMenu(searchHintRes = R.string.search_edittext_hint, showKeyboard = false)
        binding.toolbar.setNavigationOnClickListener { popBackStack() }

        binding.recyclerViewTokens.adapter = adapter

        adapter.setTokenItems(
            // todo: remove in PWN-7174
            listOf(
                SwapTokenItem.TokenSectionHeader("name 1"),
                SwapTokenItem.SwapTokenFinanceBlock(
                    "https://raw.githubusercontent.com/solana-labs/token-list/main/assets/mainnet/EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v/logo.png".toUri(),
                    "USDC",
                    "2.44",
                    "1.44"
                ),
                SwapTokenItem.SwapTokenFinanceBlock(
                    "https://raw.githubusercontent.com/solana-labs/token-list/main/assets/mainnet/EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v/logo.png".toUri(),
                    "USDC",
                    "2.44",
                    "1.44"
                ),
                SwapTokenItem.TokenSectionHeader("name 2"),
                SwapTokenItem.SwapTokenFinanceBlock(
                    "https://raw.githubusercontent.com/solana-labs/token-list/main/assets/mainnet/EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v/logo.png".toUri(),
                    "USDC",
                    "2.44",
                    "1.44"
                )
            )
        )
    }
}
