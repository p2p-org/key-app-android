package com.p2p.wallet.main.ui.main

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.p2p.wallet.R
import com.p2p.wallet.common.mvp.BaseMvpFragment
import com.p2p.wallet.databinding.FragmentMainBinding
import com.p2p.wallet.history.ui.main.TokenDetailsFragment
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.main.model.TokenItem
import com.p2p.wallet.main.model.VisibilityState
import com.p2p.wallet.main.ui.buy.BuyFragment
import com.p2p.wallet.main.ui.main.adapter.TokenAdapter
import com.p2p.wallet.main.ui.options.TokenOptionsDialog
import com.p2p.wallet.main.ui.receive.ReceiveFragment
import com.p2p.wallet.main.ui.send.SendFragment
import com.p2p.wallet.qr.ui.ScanQrFragment
import com.p2p.wallet.settings.ui.settings.SettingsFragment
import com.p2p.wallet.swap.orca.ui.OrcaSwapFragment
import com.p2p.wallet.utils.replaceFragment
import com.p2p.wallet.utils.viewbinding.viewBinding
import org.koin.android.ext.android.inject
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
            mainRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            mainRecyclerView.adapter = mainAdapter

            showPieChart(emptyList())

            settingsImageView.setOnClickListener {
                replaceFragment(SettingsFragment.create())
            }

            refreshLayout.setOnRefreshListener {
                presenter.refresh()
            }

            buyImageView.setOnClickListener {
                replaceFragment(BuyFragment.create(null))
            }

            receiveImageView.setOnClickListener {
                replaceFragment(ReceiveFragment.create(null))
            }

            sendImageView.setOnClickListener {
                replaceFragment(SendFragment.create())
            }

            swapImageView.setOnClickListener {
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

    override fun showTokens(tokens: List<TokenItem>, isZerosHidden: Boolean, state: VisibilityState) {
        mainAdapter.setItems(tokens, isZerosHidden, state)
    }

    override fun showBalance(balance: BigDecimal) {
        binding.balanceTextView.text = getString(R.string.main_usd_format, balance.toString())
    }

    override fun showChart(tokens: List<Token.Active>) {
        showPieChart(tokens)
    }

    override fun showLoading(isLoading: Boolean) {
        binding.progressView.isVisible = isLoading
    }

    override fun showRefreshing(isRefreshing: Boolean) {
        with(binding) {
            refreshLayout.isRefreshing = isRefreshing
        }
    }

    override fun onDestroy() {
        /* We are clearing cache only if activity is destroyed */
        presenter.clearCache()
        super.onDestroy()
    }

    @Suppress("MagicNumber")
    private fun showPieChart(tokens: List<Token.Active>) {
        val pieData = tokens.map { PieEntry(it.price.toFloat()) }
        val colors = tokens.map { it.color }.toIntArray()

        binding.mainPieChart.apply {
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
        replaceFragment(TokenDetailsFragment.create(token))
    }

    private fun onEditClicked(token: Token.Active) {
        TokenOptionsDialog.show(childFragmentManager, token)
    }

    private fun onHideClicked(token: Token.Active) {
        presenter.toggleVisibility(token)
    }
}