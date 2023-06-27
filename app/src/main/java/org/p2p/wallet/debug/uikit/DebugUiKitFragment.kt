package org.p2p.wallet.debug.uikit

import android.os.Bundle
import android.view.View
import org.p2p.uikit.components.informerViewDelegate
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.organisms.sectionheader.sectionHeaderCellDelegate
import org.p2p.uikit.utils.attachAdapter
import org.p2p.wallet.R
import org.p2p.wallet.common.adapter.CommonAnyCellAdapter
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentDebugUiKitBinding
import org.p2p.wallet.utils.viewbinding.viewBinding

class DebugUiKitFragment :
    BaseMvpFragment<DebugUiKitFragmentContract.View, DebugUiKitFragmentContract.Presenter>(
        R.layout.fragment_debug_ui_kit
    ),
    DebugUiKitFragmentContract.View {

    override val presenter: DebugUiKitFragmentContract.Presenter = DebugUiKitFragmentPresenter()

    private val binding: FragmentDebugUiKitBinding by viewBinding()
    private val adapter = CommonAnyCellAdapter(
        sectionHeaderCellDelegate(),
        informerViewDelegate()
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerViewUiKitViews.attachAdapter(adapter)

        presenter.buildInformerViews()
    }

    override fun showViews(items: List<AnyCellItem>) {
        adapter.items = items
    }
}
