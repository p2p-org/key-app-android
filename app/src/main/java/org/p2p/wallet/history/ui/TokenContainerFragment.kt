package org.p2p.wallet.history.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentTokenContainerBinding
import org.p2p.wallet.history.model.TabItem
import org.p2p.wallet.history.ui.history.HistoryFragment
import org.p2p.wallet.history.ui.info.TokenInfoFragment
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

class TokenContainerFragment : BaseFragment(R.layout.fragment_token_container) {

    companion object {
        private const val EXTRA_TOKEN = "EXTRA_TOKEN"
        fun create(token: Token.Active) = TokenContainerFragment().withArgs(
            EXTRA_TOKEN to token
        )
    }

    private val token: Token.Active by args(EXTRA_TOKEN)

    private val binding: FragmentTokenContainerBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.title = token.tokenName
            toolbar.setNavigationOnClickListener { popBackStack() }

            val tabItems = listOf(
                TabItem(TokenInfoFragment.create(token), getString(R.string.details_info)),
                TabItem(HistoryFragment.create(token), getString(R.string.details_history))
            )
            viewPager.adapter = TokenPagerAdapter(this@TokenContainerFragment, tabItems)

            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = tabItems[position].tabTitle
            }.attach()
        }
    }

    inner class TokenPagerAdapter(
        fragment: TokenContainerFragment,
        private val tabItems: List<TabItem>
    ) : FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment = tabItems[position].fragment
    }
}