package com.p2p.wallet.history.ui.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.p2p.wallet.R
import com.p2p.wallet.common.widget.TabItem
import com.p2p.wallet.databinding.ItemTokenDetailsHeaderBinding
import com.p2p.wallet.history.model.HistoryItem
import com.p2p.wallet.history.model.PeriodHistory
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.history.ui.main.OnHeaderClickListener
import com.p2p.wallet.main.ui.receive.ReceiveFragment
import com.p2p.wallet.utils.copyToClipBoard
import com.p2p.wallet.utils.resFromTheme

class HeaderViewHolder(
    private val binding: ItemTokenDetailsHeaderBinding,
    private val listener: OnHeaderClickListener
) : RecyclerView.ViewHolder(binding.root) {

    constructor(parent: ViewGroup, listener: OnHeaderClickListener) : this(
        ItemTokenDetailsHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        listener
    )

    fun onBind(item: HistoryItem.Header) {
        with(binding) {
            addressTextView.text = item.sol.publicKey

            val token = item.token
            setupTabsView(token, this)

            balanceTextView.text = token.getFormattedPrice()
            totalTextView.text = token.getFormattedTotal()

            qrImageView.setOnClickListener { listener.navigateToFragment(ReceiveFragment.create(token)) }
            addressView.setOnClickListener {
                it.context.copyToClipBoard(item.sol.publicKey)
                Toast.makeText(it.context, R.string.common_copied, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun setChartData(entries: List<Entry>) {
        showChartData(binding, entries)
    }

    private fun setupTabsView(token: Token, binding: ItemTokenDetailsHeaderBinding) {
        with(binding) {
            val tabs = listOf(
                TabItem(
                    PeriodHistory.ONE_HOUR.resourceId,
                    root.context.getString(PeriodHistory.ONE_HOUR.resourceId)
                ),
                TabItem(
                    PeriodHistory.FOUR_HOURS.resourceId,
                    root.context.getString(PeriodHistory.FOUR_HOURS.resourceId)
                ),
                TabItem(
                    PeriodHistory.ONE_DAY.resourceId,
                    root.context.getString(PeriodHistory.ONE_DAY.resourceId)
                ),
                TabItem(
                    PeriodHistory.ONE_WEEK.resourceId,
                    root.context.getString(PeriodHistory.ONE_WEEK.resourceId)
                ),
                TabItem(
                    PeriodHistory.ONE_MONTH.resourceId,
                    root.context.getString(PeriodHistory.ONE_MONTH.resourceId)
                ),
            )

            tabsView.onTabChanged = { tabId ->
                when (tabId) {
                    PeriodHistory.ONE_HOUR.resourceId -> listener.loadHourlyChartData(
                        token.tokenSymbol,
                        PeriodHistory.ONE_HOUR.value
                    )
                    PeriodHistory.FOUR_HOURS.resourceId -> listener.loadHourlyChartData(
                        token.tokenSymbol,
                        PeriodHistory.FOUR_HOURS.value
                    )
                    PeriodHistory.ONE_DAY.resourceId -> listener.loadDailyChartData(
                        token.tokenSymbol,
                        PeriodHistory.ONE_DAY.value
                    )
                    PeriodHistory.ONE_WEEK.resourceId -> listener.loadDailyChartData(
                        token.tokenSymbol,
                        PeriodHistory.ONE_WEEK.value
                    )
                    PeriodHistory.ONE_MONTH.resourceId -> listener.loadDailyChartData(
                        token.tokenSymbol,
                        PeriodHistory.ONE_MONTH.value
                    )
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

    private fun showChartData(binding: ItemTokenDetailsHeaderBinding, entries: List<Entry>) {
        val context = binding.root.context
        val lineDataSet = LineDataSet(entries, null)
        lineDataSet.lineWidth = 2f
        lineDataSet.setDrawCircles(false)
        lineDataSet.setDrawValues(false)
        lineDataSet.color = binding.root.resFromTheme(R.attr.colorAccentPrimary)
        lineDataSet.setDrawFilled(true)
        lineDataSet.setDrawHorizontalHighlightIndicator(false)
        lineDataSet.highLightColor = binding.root.resFromTheme(R.attr.colorAccentPrimary)
        lineDataSet.isHighlightEnabled = true

        val fillGradient = ContextCompat.getDrawable(context, R.drawable.bg_line_chart)
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
}