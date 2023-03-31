package org.p2p.wallet.history.ui.sendvialink

import androidx.recyclerview.widget.LinearLayoutManager
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.uikit.components.finance_block.FinanceBlockCellModel
import org.p2p.uikit.components.finance_block.financeBlockCellDelegate
import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.R
import org.p2p.wallet.common.adapter.CommonAnyCellAdapter
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentHistorySendLinksBinding
import org.p2p.wallet.history.ui.delegates.historyDateTextDelegate
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding

class HistorySendLinksFragment :
    BaseMvpFragment<HistorySendLinksContract.View, HistorySendLinksContract.Presenter>(
        R.layout.fragment_history_send_links
    ),
    HistorySendLinksContract.View {

    companion object {
        fun create(): HistorySendLinksFragment = HistorySendLinksFragment()
    }

    private val adapter = CommonAnyCellAdapter(
        financeBlockCellDelegate(onItemClicked = ::onItemClicked),
        historyDateTextDelegate()
    )

    override val presenter: HistorySendLinksContract.Presenter by inject()
    private val binding: FragmentHistorySendLinksBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { popBackStack() }

        binding.recyclerViewLinks.adapter = adapter
        binding.recyclerViewLinks.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun showUserLinks(userLinksModels: List<AnyCellItem>) {
        adapter.items = userLinksModels
    }

    private fun onItemClicked(item: FinanceBlockCellModel) {
        val linkUuid = item.typedPayload<String>()
        HistorySendLinkDetailsBottomSheet.show(childFragmentManager, linkUuid)
    }
}
