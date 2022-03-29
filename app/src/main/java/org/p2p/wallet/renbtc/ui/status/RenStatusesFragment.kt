package org.p2p.wallet.renbtc.ui.status

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
import org.p2p.wallet.renbtc.model.RenTransactionStatus
import org.p2p.wallet.utils.DateTimeUtils
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.attachAdapter
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.scaleMedium
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

class RenStatusesFragment :
    BaseMvpFragment<RenStatusesContract.View, RenStatusesContract.Presenter>(
        R.layout.fragment_ren_transactions
    ),
    RenStatusesContract.View {

    companion object {
        private const val EXTRA_TRANSACTION = "EXTRA_TRANSACTION"
        fun create(transaction: RenTransaction) = RenStatusesFragment().withArgs(
            EXTRA_TRANSACTION to transaction
        )
    }

    override val presenter: RenStatusesContract.Presenter by inject()
    private val binding: FragmentRenTransactionsBinding by viewBinding()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()
    private val transaction: RenTransaction by args(EXTRA_TRANSACTION)
    private val adapter: RenStatusesAdapter by lazy {
        RenStatusesAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        analyticsInteractor.logScreenOpenEvent(ScreenNames.Receive.BITCOIN_STATUS)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            toolbar.title = getTransactionTitle()

            dateTextView.text = DateTimeUtils.getFormattedDate(
                transaction.getLatestStatus()?.date ?: System.currentTimeMillis()
            )
            recyclerView.layoutManager = LinearLayoutManager(requireContext()).apply {
                reverseLayout = true
            }
            recyclerView.attachAdapter(adapter)
        }

        presenter.subscribe(transaction.transactionId)
    }

    override fun showStatuses(statuses: List<RenTransactionStatus>) {
        adapter.setItems(statuses)

        val isEmpty = statuses.isEmpty()
        binding.recyclerView.isVisible = !isEmpty
        binding.emptyTextView.isVisible = isEmpty
    }

    override fun showStatusesNotFound() {
        binding.recyclerView.isVisible = false
        binding.emptyTextView.isVisible = true
    }

    private fun getTransactionTitle(): String {
        val status = transaction.getLatestStatus()
        return if (status is RenTransactionStatus.SuccessfullyMinted) {
            getString(R.string.receive_renbtc_transaction_format, status.amount.scaleMedium())
        } else {
            getString(R.string.receive_renbtc_transaction)
        }
    }
}
