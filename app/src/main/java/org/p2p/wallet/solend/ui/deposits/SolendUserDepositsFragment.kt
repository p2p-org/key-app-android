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
import org.p2p.wallet.solend.ui.deposits.adapter.SolendDepositsAdapter
import org.p2p.wallet.solend.ui.deposits.adapter.TokenDepositItemClickListener
import org.p2p.wallet.solend.ui.earn.bottomsheet.SolendTopUpBottomSheetFragment
import org.p2p.wallet.solend.ui.withdraw.SolendWithdrawFragment
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.unsafeLazy
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_DEPOSIT_TOKENS = "ARG_DEPOSIT_TOKENS"

class SolendUserDepositsFragment :
    BaseMvpFragment<SolendUserDepositsContract.View, SolendUserDepositsContract.Presenter>(
        R.layout.fragment_solend_deposits
    ),
    SolendUserDepositsContract.View,
    TokenDepositItemClickListener {

    companion object {
        fun create(deposits: List<SolendDepositToken.Active>) = SolendUserDepositsFragment().withArgs(
            ARG_DEPOSIT_TOKENS to deposits
        )
    }

    override val presenter: SolendUserDepositsContract.Presenter by inject()

    private val binding: FragmentSolendDepositsBinding by viewBinding()

    private val deposits: List<SolendDepositToken.Active> by args(ARG_DEPOSIT_TOKENS)

    private val depositAdapter: SolendDepositsAdapter by unsafeLazy {
        SolendDepositsAdapter(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            binding.toolbar.title = getString(R.string.solend_deposits_title_with_count, deposits.size)
            toolbar.setNavigationOnClickListener {
                popBackStack()
            }
            tokensRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            tokensRecyclerView.attachAdapter(depositAdapter)

            depositAdapter.setItems(deposits)
        }
    }

    override fun showLoading(isLoading: Boolean) {
        with(binding) {
            progressBar.isVisible = isLoading
            tokensRecyclerView.isVisible = !isLoading
        }
    }

    override fun onAddMoreClicked(token: SolendDepositToken.Active) {
        SolendTopUpBottomSheetFragment.show(
            fragmentManager = parentFragmentManager,
            deposit = token
        )
    }

    override fun onWithdrawClicked(token: SolendDepositToken.Active) {
        replaceFragment(SolendWithdrawFragment.create(token, deposits))
    }
}
