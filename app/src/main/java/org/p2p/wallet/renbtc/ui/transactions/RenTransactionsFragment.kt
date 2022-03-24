package org.p2p.wallet.renbtc.ui.transactions

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentRenTransactionsBinding
import org.p2p.wallet.renbtc.model.RenTransaction
import org.p2p.wallet.renbtc.ui.status.RenStatusesFragment
import org.p2p.wallet.utils.attachAdapter
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding

class RenTransactionsFragment :
    BaseMvpFragment<RenTransactionsContract.View, RenTransactionsContract.Presenter>(
        R.layout.fragment_ren_transactions
    ),
    RenTransactionsContract.View {

    companion object {
        fun create() = RenTransactionsFragment()
    }

    override val presenter: RenTransactionsContract.Presenter by inject()
    private val binding: FragmentRenTransactionsBinding by viewBinding()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()

    private val adapter: RenTransactionsAdapter by lazy {
        RenTransactionsAdapter { presenter.onTransactionClicked(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        analyticsInteractor.logScreenOpenEvent(ScreenNames.Receive.BITCOIN_STATUSES)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            dateTextView.isVisible = false

            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.attachAdapter(adapter)
        }

        presenter.loadTransactions()
    }

    override fun showTransactions(transactions: List<RenTransaction>) {
        adapter.setItems(transactions)

        val isEmpty = transactions.isEmpty()
        binding.recyclerView.isVisible = !isEmpty
        binding.emptyTextView.isVisible = isEmpty
    }

    override fun showTransaction(item: RenTransaction) {
        replaceFragment(RenStatusesFragment.create(item))
    }
}
