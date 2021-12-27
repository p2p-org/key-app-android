package org.p2p.wallet.main.ui.main

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import org.koin.android.ext.android.inject
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.ReserveMode
import org.p2p.wallet.auth.ui.username.ReserveUsernameFragment
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentMainBinding
import org.p2p.wallet.history.ui.TokenContainerFragment
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.main.model.TokenItem
import org.p2p.wallet.main.model.VisibilityState
import org.p2p.wallet.main.ui.buy.moonpay.BuySolanaFragment
import org.p2p.wallet.main.ui.main.adapter.TokenAdapter
import org.p2p.wallet.main.ui.options.TokenOptionsDialog
import org.p2p.wallet.main.ui.receive.ReceiveFragment
import org.p2p.wallet.main.ui.send.SendFragment
import org.p2p.wallet.main.ui.username.UsernameConfirmationDialog
import org.p2p.wallet.qr.ui.ScanQrFragment
import org.p2p.wallet.settings.ui.settings.SettingsFragment
import org.p2p.wallet.swap.ui.orca.OrcaSwapFragment
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import java.math.BigDecimal

class MainFragment :
    BaseMvpFragment<MainContract.View, MainContract.Presenter>(R.layout.fragment_main),
    MainContract.View {

    companion object {
        fun create() = MainFragment()
    }

    override val presenter: MainContract.Presenter by inject()

    private val binding: FragmentMainBinding by viewBinding()

    private val mainAdapter: TokenAdapter by lazy {
        TokenAdapter(
            onItemClicked = { onTokenClicked(it) },
            onEditClicked = { onEditClicked(it) },
            onHideClicked = { onHideClicked(it) },
            onToggleClicked = { presenter.toggleVisibilityState() }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            val linearLayoutManager = LinearLayoutManager(requireContext())
            mainRecyclerView.layoutManager = linearLayoutManager
            mainRecyclerView.adapter = mainAdapter
            mainRecyclerView.isNestedScrollingEnabled = true

            showPieChart(emptyList())

            bannerViewContainer.bannerImageView.clipToOutline = true

            bannerViewContainer.reserveButton.setOnClickListener {
                replaceFragment(ReserveUsernameFragment.create(ReserveMode.POP))
            }

            bannerViewContainer.cancelButton.setOnClickListener {
                UsernameConfirmationDialog.show(
                    fm = childFragmentManager,
                    titleRes = R.string.main_hide_banner_confirm_question,
                    subTitleRes = R.string.main_hide_banner_confirm_body,
                    primaryButtonRes = R.string.common_proceed,
                    secondaryButtonRes = R.string.common_proceed_do_not_show,
                    onPrimaryButtonClicked = { presenter.hideUsernameBanner() },
                    onSecondaryButtonClicked = { presenter.hideUsernameBanner(forever = true) }
                )
            }

            settingsImageView.setOnClickListener {
                replaceFragment(SettingsFragment.create())
            }

            swipeRefreshLayout.setOnRefreshListener {
                presenter.refresh()
            }

            // TODO: temporary hiding moonpay for release
            headerViewContainer.buyButton.isVisible = BuildConfig.DEBUG
            headerViewContainer.buyDivider.isVisible = BuildConfig.DEBUG

            headerViewContainer.buyButton.setOnClickListener {
                replaceFragment(BuySolanaFragment.create())
            }

            headerViewContainer.receiveButton.setOnClickListener {
                replaceFragment(ReceiveFragment.create(null))
            }

            headerViewContainer.sendButton.setOnClickListener {
                replaceFragment(SendFragment.create())
            }

            headerViewContainer.swapButton.setOnClickListener {
                replaceFragment(OrcaSwapFragment.create())
            }

            scanImageView.setOnClickListener {
                val target = ScanQrFragment.create(
                    successCallback = { replaceFragment(SendFragment.create(it)) }
                )
                replaceFragment(target)
            }
        }

        presenter.collectData()
    }

    override fun showUsernameBanner(isVisible: Boolean) {
        binding.bannerViewContainer.bannerView.isVisible = isVisible
    }

    override fun showTokens(tokens: List<TokenItem>, isZerosHidden: Boolean, state: VisibilityState) {
        mainAdapter.setItems(tokens, isZerosHidden, state)
    }

    override fun showBalance(balance: BigDecimal) {
        binding.headerViewContainer.balanceTextView.text = getString(R.string.main_usd_format, balance.toString())
    }

    override fun showChart(tokens: List<Token.Active>) {
        showPieChart(tokens)
    }

    override fun showLoading(isLoading: Boolean) {
//        binding.progressView.isVisible = isLoading
        binding.swipeRefreshLayout.isRefreshing = isLoading
    }

    override fun showRefreshing(isRefreshing: Boolean) {
        binding.swipeRefreshLayout.isRefreshing = isRefreshing
    }

    override fun onDestroy() {
        /* We are clearing cache only if activity is destroyed */
        presenter.clearCache()
        super.onDestroy()
    }

    @Suppress("MagicNumber")
    private fun showPieChart(tokens: List<Token.Active>) {
        val pieData = tokens.map { PieEntry(it.totalInUsd?.toFloat() ?: 0f) }
        val colors = tokens.map { it.color }.toIntArray()

        binding.headerViewContainer.mainPieChart.apply {
            val dataSet = PieDataSet(pieData, null)
            dataSet.sliceSpace = 1f
            dataSet.selectionShift = 15f
            dataSet.setColors(colors, context)

            val data = PieData(dataSet)
            data.setDrawValues(false)
            this.data = data

            setUsePercentValues(true)
            setTouchEnabled(false)
            setHoleColor(Color.WHITE)
            setDrawCenterText(false)
            animateY(500)
            setDrawEntryLabels(false)
            description.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 70f
            legend.isEnabled = false
            invalidate()
        }
    }

    private fun onTokenClicked(token: Token.Active) {
        replaceFragment(TokenContainerFragment.create(token))
    }

    private fun onEditClicked(token: Token.Active) {
        TokenOptionsDialog.show(childFragmentManager, token)
    }

    private fun onHideClicked(token: Token.Active) {
        presenter.toggleVisibility(token)
    }
}