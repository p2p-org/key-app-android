package org.p2p.wallet.home.ui.main

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.ReserveMode
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.auth.ui.username.ReserveUsernameFragment
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.widget.ActionButtonsView
import org.p2p.wallet.common.ui.widget.OnOffsetChangedListener
import org.p2p.wallet.databinding.FragmentHomeBinding
import org.p2p.wallet.history.ui.history.HistoryFragment
import org.p2p.wallet.home.analytics.BrowseAnalytics
import org.p2p.wallet.home.model.HomeElementItem
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.home.model.VisibilityState
import org.p2p.wallet.home.ui.main.adapter.OnHomeItemsClickListener
import org.p2p.wallet.home.ui.main.adapter.TokenAdapter
import org.p2p.wallet.home.ui.select.bottomsheet.SelectTokenBottomSheet
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.moonpay.ui.BuySolanaFragment
import org.p2p.wallet.receive.solana.ReceiveSolanaFragment
import org.p2p.wallet.send.ui.main.SendFragment
import org.p2p.wallet.swap.ui.orca.OrcaSwapFragment
import org.p2p.wallet.utils.SpanUtils
import org.p2p.wallet.utils.getColor
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import java.math.BigDecimal
import kotlin.math.absoluteValue

private const val KEY_RESULT_TOKEN = "KEY_RESULT_TOKEN"
private const val KEY_REQUEST_TOKEN = "KEY_REQUEST_TOKEN"

class HomeFragment :
    BaseMvpFragment<HomeContract.View, HomeContract.Presenter>(R.layout.fragment_home),
    HomeContract.View,
    OnHomeItemsClickListener {

    companion object {
        fun create() = HomeFragment()
    }

    override val presenter: HomeContract.Presenter by inject()

    private val binding: FragmentHomeBinding by viewBinding()

    private val mainAdapter: TokenAdapter by lazy {
        TokenAdapter(this)
    }

    private val browseAnalytics: BrowseAnalytics by inject()
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
                    presenter.onBuyClicked()
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

            appBarLayout.addOnOffsetChangedListener(
                AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
                    val offset = (verticalOffset.toFloat() / appBarLayout.height).absoluteValue
                    (binding.actionButtonsView as? OnOffsetChangedListener)?.onOffsetChanged(offset)
                }
            )
        }

        childFragmentManager.setFragmentResultListener(
            KEY_REQUEST_TOKEN,
            viewLifecycleOwner
        ) { _, result ->
            when {
                result.containsKey(KEY_RESULT_TOKEN) -> {
                    val token = result.getParcelable<Token>(KEY_RESULT_TOKEN)
                    if (token != null) replaceFragment(BuySolanaFragment.create(token))
                }
            }
        }

        presenter.collectData()
    }

    override fun showTokens(tokens: List<HomeElementItem>, isZerosHidden: Boolean, state: VisibilityState) {
        mainAdapter.setItems(tokens, isZerosHidden, state)
    }

    override fun showTokensForBuy(tokens: List<Token>) {
        SelectTokenBottomSheet.show(childFragmentManager, tokens, KEY_REQUEST_TOKEN, KEY_RESULT_TOKEN)
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
        emptyStateLayout.isVisible = isEmpty
        swipeRefreshLayout.isVisible = !isEmpty
        balanceTextView.isVisible = !isEmpty
        balanceLabelTextView.isVisible = !isEmpty
    }

    override fun onDestroy() {
        /* We are clearing cache only if activity is destroyed */
        presenter.clearCache()
        super.onDestroy()
    }

    override fun onBannerClicked(bannerId: Int) {
        when (bannerId) {
            R.string.main_username_banner_option -> {
                browseAnalytics.logBannerUsernamePressed()
                replaceFragment(ReserveUsernameFragment.create(ReserveMode.POP))
            }
            R.string.main_feedback_banner_option -> {
                browseAnalytics.logBannerFeedbackPressed()
                IntercomService.showMessenger()
            }
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