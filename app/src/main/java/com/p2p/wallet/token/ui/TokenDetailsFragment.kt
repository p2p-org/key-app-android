package com.p2p.wallet.token.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.p2p.wallet.R
import com.p2p.wallet.common.mvp.BaseMvpFragment
import com.p2p.wallet.common.recycler.EndlessScrollListener
import com.p2p.wallet.common.widget.TabItem
import com.p2p.wallet.databinding.FragmentTokenDetailsBinding
import com.p2p.wallet.main.ui.options.TokenOptionsDialog
import com.p2p.wallet.main.ui.receive.ReceiveFragment
import com.p2p.wallet.main.ui.send.SendFragment
import com.p2p.wallet.swap.ui.SwapFragment
import com.p2p.wallet.token.model.PeriodHistory.FOUR_HOURS
import com.p2p.wallet.token.model.PeriodHistory.ONE_DAY
import com.p2p.wallet.token.model.PeriodHistory.ONE_HOUR
import com.p2p.wallet.token.model.PeriodHistory.ONE_MONTH
import com.p2p.wallet.token.model.PeriodHistory.ONE_WEEK
import com.p2p.wallet.token.model.Token
import com.p2p.wallet.token.model.Transaction
import com.p2p.wallet.token.ui.adapter.HistoryAdapter
import com.p2p.wallet.utils.addFragment
import com.p2p.wallet.utils.args
import com.p2p.wallet.utils.copyToClipBoard
import com.p2p.wallet.utils.popBackStack
import com.p2p.wallet.utils.replaceFragment
import com.p2p.wallet.utils.resFromTheme
import com.p2p.wallet.utils.showInfoDialog
import com.p2p.wallet.utils.viewbinding.viewBinding
import com.p2p.wallet.utils.withArgs
import org.koin.android.ext.android.inject

class TokenDetailsFragment :
    BaseMvpFragment<TokenDetailsContract.View, TokenDetailsContract.Presenter>(R.layout.fragment_token_details),
    TokenDetailsContract.View {

    companion object {
        private const val EXTRA_TOKEN = "EXTRA_TOKEN"

        fun create(token: Token) =
            TokenDetailsFragment().withArgs(
                EXTRA_TOKEN to token
            )
    }

    override val presenter: TokenDetailsContract.Presenter by inject()

    private val token: Token by args(EXTRA_TOKEN)

    private val binding: FragmentTokenDetailsBinding by viewBinding()

    private val historyAdapter: HistoryAdapter by lazy {
        HistoryAdapter(
            onTransactionClicked = {
                // todo: Add implementation
            },
            onRetryClicked = {
                // todo: Add implementation
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            setupTabsView()
            if (!token.isSOL) {
                toolbar.inflateMenu(R.menu.menu_token_details)
                toolbar.setOnMenuItemClickListener {
                    if (it.itemId == R.id.settingsItem) {
                        TokenOptionsDialog.show(childFragmentManager, token)
                        return@setOnMenuItemClickListener true
                    }

                    return@setOnMenuItemClickListener false
                }
            }
            toolbar.title = token.tokenSymbol
            toolbar.subtitle = token.tokenName
            balanceTextView.text = token.getFormattedPrice()
            totalTextView.text = token.getFormattedTotal()

            toolbar.setNavigationOnClickListener { popBackStack() }
            qrImageView.setOnClickListener { replaceFragment(ReceiveFragment.create(token)) }
            addressView.setOnClickListener {
                requireContext().copyToClipBoard(token.publicKey)
                Toast.makeText(requireContext(), R.string.common_copied, Toast.LENGTH_SHORT).show()
            }

            receiveImageView.setOnClickListener {
                addFragment(ReceiveFragment.create(token))
            }

            sendImageView.setOnClickListener {
                addFragment(SendFragment.create(token))
            }

            swapImageView.setOnClickListener {
                addFragment(SwapFragment.create(token))
            }

            with(historyRecyclerView) {
                val linearLayoutManager = LinearLayoutManager(requireContext())
                layoutManager = linearLayoutManager
                adapter = historyAdapter

                val scrollListener = EndlessScrollListener(linearLayoutManager) { total ->
                    presenter.loadHistory(token.publicKey, total, token.tokenSymbol)
                }

                doOnAttach {
                    addOnScrollListener(scrollListener)
                }
                doOnDetach {
                    removeOnScrollListener(scrollListener)
                }
            }
        }

        presenter.loadHistory(token.publicKey, null, token.tokenSymbol)
        presenter.loadSolAddress()
    }

    override fun showSolAddress(sol: Token) {
        binding.addressTextView.text = sol.publicKey
    }

    override fun showError(@StringRes resId: Int, argument: String) {
        showInfoDialog(getString(resId, argument))
    }

    override fun showHistory(transactions: List<Transaction>) {
        historyAdapter.setData(transactions)

        val isEmpty = transactions.isEmpty()
        binding.historyRecyclerView.isVisible = !isEmpty
        binding.emptyGroup.isVisible = isEmpty
    }

    override fun showChartData(entries: List<Entry>) {
        val lineDataSet = LineDataSet(entries, null)
        lineDataSet.lineWidth = 2f
        lineDataSet.setDrawCircles(false)
        lineDataSet.setDrawValues(false)
        lineDataSet.color = resFromTheme(R.attr.colorAccentPrimary)
        lineDataSet.setDrawFilled(true)
        lineDataSet.setDrawHorizontalHighlightIndicator(false)
        lineDataSet.highLightColor = resFromTheme(R.attr.colorAccentPrimary)
        lineDataSet.isHighlightEnabled = true

        val fillGradient = ContextCompat.getDrawable(requireContext(), R.drawable.bg_line_chart)
        lineDataSet.fillDrawable = fillGradient

        binding.lineChart.apply {
            setViewPortOffsets(0f, 0f, 0f, 0f)
            description = Description().apply { text = "" }
            setDrawBorders(false)
            setBorderColor(resFromTheme(R.attr.colorAccentPrimary))
            axisRight.setDrawGridLines(false)
            axisLeft.setDrawGridLines(false)
            xAxis.setDrawGridLines(false)
            axisLeft.setDrawLabels(false)
            axisRight.setDrawLabels(false)
            xAxis.setDrawLabels(false)
            setTouchEnabled(true)
            legend.isEnabled = false
            val mv = MarkerView(context, R.layout.view_line_chart_dot)
            mv.setOffset(
                (-mv.measuredWidth / 2).toFloat(),
                (-mv.measuredHeight).toFloat() / 2
            )
            marker = mv
            data = LineData(lineDataSet)
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onNothingSelected() {}
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    highlightValue(h)
                }
            })
            invalidate()
            animateX(500)
        }
    }

    override fun showLoading(isLoading: Boolean) {
        binding.progressView.isVisible = isLoading
    }

    private fun setupTabsView() {
        with(binding) {
            val tabs = listOf(
                TabItem(ONE_HOUR.resourceId, getString(ONE_HOUR.resourceId)),
                TabItem(FOUR_HOURS.resourceId, getString(FOUR_HOURS.resourceId)),
                TabItem(ONE_DAY.resourceId, getString(ONE_DAY.resourceId)),
                TabItem(ONE_WEEK.resourceId, getString(ONE_WEEK.resourceId)),
                TabItem(ONE_MONTH.resourceId, getString(ONE_MONTH.resourceId)),
            )

            tabsView.onTabChanged = { tabId ->
                when (tabId) {
                    ONE_HOUR.resourceId -> presenter.loadHourlyChartData(token.tokenSymbol, ONE_HOUR.value)
                    FOUR_HOURS.resourceId -> presenter.loadHourlyChartData(token.tokenSymbol, FOUR_HOURS.value)
                    ONE_DAY.resourceId -> presenter.loadDailyChartData(token.tokenSymbol, ONE_DAY.value)
                    ONE_WEEK.resourceId -> presenter.loadDailyChartData(token.tokenSymbol, ONE_WEEK.value)
                    ONE_MONTH.resourceId -> presenter.loadDailyChartData(token.tokenSymbol, ONE_MONTH.value)
                }
            }

            tabsView.setTabs(
                tabs = tabs,
                defaultTab = TabItem(ONE_HOUR.resourceId, getString(ONE_HOUR.resourceId))
            )
        }
    }
}