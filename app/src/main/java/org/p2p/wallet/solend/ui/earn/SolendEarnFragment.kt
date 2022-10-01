package org.p2p.wallet.solend.ui.earn

import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.uikit.utils.attachAdapter
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSolendEarnBinding
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.solend.ui.earn.adapter.SolendEarnAdapter
import org.p2p.wallet.utils.viewbinding.viewBinding

class SolendEarnFragment :
    BaseMvpFragment<SolendEarnContract.View, SolendEarnContract.Presenter>(R.layout.fragment_solend_earn),
    SolendEarnContract.View {

    companion object {
        fun create() = SolendEarnFragment()
    }

    override val presenter: SolendEarnContract.Presenter by inject()

    private val earnAdapter: SolendEarnAdapter by lazy {
        SolendEarnAdapter()
    }

    private val binding: FragmentSolendEarnBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            tokensRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            tokensRecyclerView.attachAdapter(earnAdapter)
        }

        presenter.load()
    }

    override fun showTokens(tokens: List<SolendDepositToken>) {
        earnAdapter.setItems(tokens)
    }

    override fun showLoading(isLoading: Boolean) {
        with(binding) {
            progressBar.isVisible = isLoading
            tokensRecyclerView.isVisible = !isLoading
        }
    }
}
