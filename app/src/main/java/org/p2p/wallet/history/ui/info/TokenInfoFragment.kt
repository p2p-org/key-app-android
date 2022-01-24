package org.p2p.wallet.history.ui.info

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.glide.GlideManager
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.widget.TabItem
import org.p2p.wallet.databinding.FragmentInfoTokenBinding
import org.p2p.wallet.history.model.PeriodHistory
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.main.ui.receive.ReceiveFragment
import org.p2p.wallet.main.ui.send.SendFragment
import org.p2p.wallet.swap.ui.orca.OrcaSwapFragment
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.colorFromTheme
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.showErrorDialog
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import org.p2p.wallet.utils.withTextOrGone

class TokenInfoFragment :
    BaseMvpFragment<TokenInfoContract.View, TokenInfoContract.Presenter>(R.layout.fragment_info_token),
    TokenInfoContract.View {

    companion object {
        private const val EXTRA_TOKEN = "EXTRA_TOKEN"
        fun create(token: Token.Active) = TokenInfoFragment().withArgs(
            EXTRA_TOKEN to token
        )
    }

    private val token: Token.Active by args(EXTRA_TOKEN)

    override val presenter: TokenInfoContract.Presenter by inject()

    private val glideManager: GlideManager by inject()

    private val binding: FragmentInfoTokenBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            glideManager.load(tokenImageView, token.logoUrl)
            totalTextView.text = token.getFormattedTotal()
            usdTotalTextView.text = token.getFormattedUsdTotal()

            periodPriceTextView withTextOrGone token.getCurrentPrice()
            // todo: periodPercentTextView show percent

            showAddressButton.setOnClickListener {
                replaceFragment(ReceiveFragment.create(token))
            }

            receiveButton.setOnClickListener {
                replaceFragment(ReceiveFragment.create(token))
            }

            sendButton.setOnClickListener {
                replaceFragment(SendFragment.create(token))
            }

            swapButton.setOnClickListener {
                replaceFragment(OrcaSwapFragment.create(token))
            }

            setupTabsView()
        }
    }

    override fun showChartData(entries: List<Entry>) {
        val lineDataSet = LineDataSet(entries, null)
        lineDataSet.lineWidth = 2f
        lineDataSet.setDrawCircles(false)
        lineDataSet.setDrawValues(false)
        lineDataSet.color = colorFromTheme(R.attr.colorAccentGraph)
        lineDataSet.setDrawFilled(true)
        lineDataSet.setDrawHorizontalHighlightIndicator(false)
        lineDataSet.isHighlightEnabled = true

        val fillGradient = ContextCompat.getDrawable(requireContext(), R.drawable.bg_line_chart)
        lineDataSet.fillDrawable = fillGradient

        binding.lineChart.apply {
            setGridBackgroundColor(colorFromTheme(R.attr.colorAccentGraph))
            setViewPortOffsets(0f, 0f, 0f, 0f)
            description = Description().apply { text = "" }
            setDrawBorders(false)
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

    override fun showError(@StringRes resId: Int, argument: String) {
        showErrorDialog(getString(resId, argument))
    }

    override fun showLoading(isLoading: Boolean) {
        binding.progressView.isVisible = isLoading
    }

    private fun setupTabsView() {
        with(binding) {
            val tabs = listOf(
                TabItem(
                    PeriodHistory.ONE_HOUR.resourceId,
                    getString(PeriodHistory.ONE_HOUR.resourceId)
                ),
                TabItem(
                    PeriodHistory.FOUR_HOURS.resourceId,
                    getString(PeriodHistory.FOUR_HOURS.resourceId)
                ),
                TabItem(
                    PeriodHistory.ONE_DAY.resourceId,
                    getString(PeriodHistory.ONE_DAY.resourceId)
                ),
                TabItem(
                    PeriodHistory.ONE_WEEK.resourceId,
                    getString(PeriodHistory.ONE_WEEK.resourceId)
                ),
                TabItem(
                    PeriodHistory.ONE_MONTH.resourceId,
                    getString(PeriodHistory.ONE_MONTH.resourceId)
                ),
            )

            tabsView.onTabChanged = { tabId ->
                when (val period = PeriodHistory.parse(tabId)) {
                    PeriodHistory.ONE_HOUR,
                    PeriodHistory.FOUR_HOURS ->
                        presenter.loadHourlyChartData(token.tokenSymbol, period.value)
                    PeriodHistory.ONE_DAY,
                    PeriodHistory.ONE_WEEK,
                    PeriodHistory.ONE_MONTH ->
                        presenter.loadDailyChartData(token.tokenSymbol, period.value)
                }
            }

            tabsView.setTabs(
                tabs = tabs,
                defaultTab = TabItem(
                    PeriodHistory.ONE_HOUR.resourceId,
                    root.context.getString(PeriodHistory.ONE_HOUR.resourceId)
                )
            )
        }
    }
}