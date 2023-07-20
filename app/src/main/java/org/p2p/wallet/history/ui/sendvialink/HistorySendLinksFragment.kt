package org.p2p.wallet.history.ui.sendvialink

import androidx.activity.addCallback
import androidx.recyclerview.widget.LinearLayoutManager
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.uikit.components.finance_block.MainCellModel
import org.p2p.uikit.components.finance_block.mainCellDelegate
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.attachAdapter
import org.p2p.uikit.utils.recycler.decoration.groupedRoundingMainCellDecoration
import org.p2p.wallet.R
import org.p2p.wallet.common.adapter.CommonAnyCellAdapter
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentHistorySendLinksBinding
import org.p2p.wallet.history.analytics.HistoryAnalytics
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

    private val historyAnalytics: HistoryAnalytics by inject()

    private val adapter = CommonAnyCellAdapter(
        mainCellDelegate(inflateListener = {
            it.setOnClickAction { _, item -> onItemClicked(item) }
        }),
        historyDateTextDelegate()
    )

    override val presenter: HistorySendLinksContract.Presenter by inject()
    private val binding: FragmentHistorySendLinksBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            onBackPressed()
        }

        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        binding.recyclerViewLinks.attachAdapter(adapter)
        binding.recyclerViewLinks.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewLinks.addItemDecoration(groupedRoundingMainCellDecoration())
    }

    override fun showUserLinks(userLinksModels: List<AnyCellItem>) {
        adapter.items = userLinksModels
        binding.recyclerViewLinks.invalidateItemDecorations()
    }

    private fun onBackPressed() {
        popBackStack()
    }

    private fun onItemClicked(item: MainCellModel) {
        historyAnalytics.logUserSendLinkClicked()

        val linkUuid = item.typedPayload<String>()
        HistorySendLinkDetailsBottomSheet.show(childFragmentManager, linkUuid)
    }
}
