package com.p2p.wallet.main.ui.main

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.data.PieEntry
import com.p2p.wallet.R
import com.p2p.wallet.common.mvp.BaseMvpFragment
import com.p2p.wallet.dashboard.model.local.Token
import com.p2p.wallet.databinding.FragmentMainBinding
import com.p2p.wallet.main.ui.main.adapter.MainAdapter
import com.p2p.wallet.main.ui.swap.SwapFragment
import com.p2p.wallet.utils.drawChart
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

    private val mainAdapter: MainAdapter by lazy {
        MainAdapter { onTokenClicked(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            mainRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            mainRecyclerView.adapter = mainAdapter

            pieCharView.drawChart(emptyList())

            refreshLayout.setOnRefreshListener {
                presenter.loadData(true)
            }

            receiveImageView.setOnClickListener {
            }

            sendImageView.setOnClickListener {
            }

            swapImageView.setOnClickListener {
                replaceFragment(SwapFragment.create())
            }
        }

        presenter.loadData(false)
    }

    override fun showData(tokens: List<Token>, balance: BigDecimal) {
        with(binding) {
            balanceTextView.text = getString(R.string.main_usd_format, balance.toString())
            mainAdapter.setItems(tokens)

            val pieCharts = tokens.map { PieEntry(it.total.toFloat()) }
            pieCharView.drawChart(pieCharts)

            val isEmpty = tokens.isEmpty()
            mainRecyclerView.isVisible = !isEmpty
            emptyTextView.isVisible = isEmpty
        }
    }

    override fun showLoading(isLoading: Boolean) {
        with(binding) {
            if (!isLoading) refreshLayout.isRefreshing = false
            progressBar.isVisible = isLoading
            mainRecyclerView.isVisible = !isLoading
        }
    }

    private fun onTokenClicked(token: Token) {
    }
}