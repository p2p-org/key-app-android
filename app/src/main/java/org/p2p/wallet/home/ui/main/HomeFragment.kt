package org.p2p.wallet.home.ui.main

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.ReserveMode
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.auth.ui.username.ReserveUsernameFragment
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.widget.ActionButtonsView
import org.p2p.wallet.databinding.FragmentMainBinding
import org.p2p.wallet.history.ui.history.HistoryFragment
import org.p2p.wallet.home.model.HomeElementItem
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.home.model.VisibilityState
import org.p2p.wallet.home.ui.main.adapter.OnHomeItemsClickListener
import org.p2p.wallet.home.ui.main.adapter.TokenAdapter
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.moonpay.ui.BuySolanaFragment
import org.p2p.wallet.receive.solana.ReceiveSolanaFragment
import org.p2p.wallet.send.ui.SendFragment
import org.p2p.wallet.swap.ui.orca.OrcaSwapFragment
import org.p2p.wallet.utils.SpanUtils
import org.p2p.wallet.utils.getColor
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.setVisible
import org.p2p.wallet.utils.viewbinding.viewBinding
import java.math.BigDecimal

class HomeFragment :
    BaseMvpFragment<HomeContract.View, HomeContract.Presenter>(R.layout.fragment_main),
    HomeContract.View,
    OnHomeItemsClickListener {

    companion object {
        fun create() = HomeFragment()
    }

    override val presenter: HomeContract.Presenter by inject()

    private val binding: FragmentMainBinding by viewBinding()

    private val mainAdapter: TokenAdapter by lazy {
        TokenAdapter(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            val commonTitle = getString(R.string.app_name)
            val beta = getString(R.string.common_beta)
            val color = getColor(R.color.textIconSecondary)
            titleTextView.text = SpanUtils.highlightText("$commonTitle $beta", beta, color)

            mainRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            mainRecyclerView.adapter = mainAdapter

            with(actionButtonsView) {
                onBuyItemClickListener = {
                    replaceFragment(BuySolanaFragment.create())
                }
                onReceiveItemClickListener = {
                    replaceFragment(ReceiveSolanaFragment.create(null))
                }
                onSendClickListener = {
                    replaceFragment(SendFragment.create())
                }
                onSwapItemClickListener = {
                    replaceFragment(OrcaSwapFragment.create())
                }
            }

            swipeRefreshLayout.setOnRefreshListener {
                presenter.refresh()
            }
        }

        presenter.collectData()
    }

    override fun showTokens(tokens: List<HomeElementItem>, isZerosHidden: Boolean, state: VisibilityState) {
        mainAdapter.setItems(tokens, isZerosHidden, state)
    }

    override fun showBalance(balance: BigDecimal, username: Username?) {
        binding.balanceTextView.text = getString(R.string.main_usd_format, balance.toString())
        if (username == null) {
            binding.balanceLabelTextView.setText(R.string.main_balance)
        } else {
            val commonText = username.getFullUsername(requireContext())
            val color = getColor(R.color.textIconPrimary)
            binding.balanceLabelTextView.text = SpanUtils.highlightText(commonText, username.username, color)
        }
    }

    override fun showRefreshing(isRefreshing: Boolean) {
        binding.swipeRefreshLayout.isRefreshing = isRefreshing
    }

    override fun showActions(items: List<ActionButtonsView.ActionButton>) {
        binding.actionButtonsView.setItems(items)
    }

    override fun showEmptyState(isEmpty: Boolean) = with(binding) {
        emptyStateLayout.setVisible(isEmpty)
        swipeRefreshLayout.setVisible(!isEmpty)
        balanceTextView.setVisible(!isEmpty)
        balanceLabelTextView.setVisible(!isEmpty)
    }

    override fun onDestroy() {
        /* We are clearing cache only if activity is destroyed */
        presenter.clearCache()
        super.onDestroy()
    }

    override fun onBannerClicked(bannerId: Int) {
        when (bannerId) {
            R.string.main_username_banner_option -> replaceFragment(ReserveUsernameFragment.create(ReserveMode.POP))
            R.string.main_feedback_banner_option -> IntercomService.showMessenger()
        }
    }

    override fun onToggleClicked() {
        presenter.toggleVisibilityState()
    }

    override fun onTokenClicked(token: Token.Active) {
        replaceFragment(HistoryFragment.create(token))
    }

    override fun onHideClicked(token: Token.Active) {
        presenter.toggleVisibility(token)
    }
}