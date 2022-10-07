package org.p2p.wallet.solend.ui.earn

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import org.koin.android.ext.android.inject
import org.p2p.uikit.utils.attachAdapter
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.widget.earnwidget.EarnWidgetState
import org.p2p.wallet.databinding.FragmentSolendEarnBinding
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.solend.ui.deposits.SolendUserDepositsFragment
import org.p2p.wallet.solend.ui.earn.adapter.SolendEarnAdapter
import org.p2p.wallet.solend.ui.earn.bottomsheet.SolendTopUpBottomSheetFragment
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.unsafeLazy
import org.p2p.wallet.utils.viewbinding.viewBinding

class SolendEarnFragment :
    BaseMvpFragment<SolendEarnContract.View, SolendEarnContract.Presenter>(R.layout.fragment_solend_earn),
    SolendEarnContract.View {

    companion object {
        fun create() = SolendEarnFragment()
    }

    override val presenter: SolendEarnContract.Presenter by inject()

    private val binding: FragmentSolendEarnBinding by viewBinding()

    private val earnAdapter: SolendEarnAdapter by unsafeLazy {
        SolendEarnAdapter {
            presenter.onDepositTokenClicked(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            tokensRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            tokensRecyclerView.attachAdapter(earnAdapter)
            refreshLayout.setOnRefreshListener { presenter.refresh() }
        }

        presenter.load()
    }

    override fun showAvailableDeposits(deposits: List<SolendDepositToken>) {
        earnAdapter.setItems(deposits)
    }

    override fun showDepositTopUp(deposit: SolendDepositToken) {
        SolendTopUpBottomSheetFragment.show(
            fragmentManager = parentFragmentManager,
            deposit = deposit
        )
    }

    override fun showLoading(isLoading: Boolean) {
        with(binding) {
            shimmerView.root.isVisible = isLoading
            tokensRecyclerView.isVisible = !isLoading
        }
    }

    override fun setRatesErrorVisibility(isVisible: Boolean) {
        binding.root.post {
            TransitionManager.beginDelayedTransition(binding.root)
            binding.viewErrorState.isVisible = isVisible
        }
    }

    override fun showRefreshing(isRefreshing: Boolean) {
        with(binding) {
            refreshLayout.isRefreshing = isRefreshing
        }
    }

    override fun navigateToUserDeposits(deposits: List<SolendDepositToken.Active>) {
        replaceFragment(SolendUserDepositsFragment.create(deposits))
    }

    override fun showWidgetState(state: EarnWidgetState) {
        binding.viewEarnWidget.setWidgetState(state)
    }

    override fun bindWidgetActionButton(callback: () -> Unit) {
        binding.viewEarnWidget.setOnButtonClickListener { callback() }
    }
}
