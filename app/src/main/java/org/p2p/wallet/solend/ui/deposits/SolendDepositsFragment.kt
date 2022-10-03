package org.p2p.wallet.solend.ui.deposits

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.android.ext.android.inject
import org.p2p.uikit.utils.attachAdapter
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSolendDepositsBinding
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.solend.ui.deposits.adapter.DepositClickListener
import org.p2p.wallet.solend.ui.deposits.adapter.SolendDepositsAdapter
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.unsafeLazy
import org.p2p.wallet.utils.viewbinding.viewBinding

class SolendDepositsFragment :
    BaseMvpFragment<SolendDepositsContract.View, SolendDepositsContract.Presenter>(R.layout.fragment_solend_deposits),
    SolendDepositsContract.View {

    companion object {
        fun create() = SolendDepositsFragment()
    }

    override val presenter: SolendDepositsContract.Presenter by inject()

    private val binding: FragmentSolendDepositsBinding by viewBinding()

    private val depositAdapter: SolendDepositsAdapter by unsafeLazy {
        SolendDepositsAdapter(
            object : DepositClickListener {
                override fun onAddMoreClicked(token: SolendDepositToken) {
                    presenter.onAddMoreClicked(token)
                }

                override fun onWithdrawClicked(token: SolendDepositToken) {
                    presenter.onWithdrawClicked(token)
                }
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener {
                popBackStack()
            }
            tokensRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            tokensRecyclerView.attachAdapter(depositAdapter)
        }
    }

    override fun showTokens(tokens: List<SolendDepositToken>) {
        depositAdapter.setItems(tokens)
        binding.toolbar.title = getString(R.string.deposits_title_with_count, tokens.size)
    }

    override fun showLoading(isLoading: Boolean) {
        with(binding) {
            progressBar.isVisible = isLoading
            tokensRecyclerView.isVisible = !isLoading
        }
    }
}
