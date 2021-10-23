package org.p2p.wallet.renbtc.ui.statuses

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentReceivingStatusesBinding
import org.p2p.wallet.renbtc.model.RenVMStatus
import org.p2p.wallet.utils.attachAdapter
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.koin.android.ext.android.inject

class ReceivingStatusesFragment :
    BaseMvpFragment<ReceivingStatusesContract.View, ReceivingStatusesContract.Presenter>(
        R.layout.fragment_receiving_statuses
    ),
    ReceivingStatusesContract.View {

    companion object {
        fun create() = ReceivingStatusesFragment()
    }

    override val presenter: ReceivingStatusesContract.Presenter by inject()

    private val binding: FragmentReceivingStatusesBinding by viewBinding()

    private val adapter: ReceiveStatusesAdapter by lazy {
        ReceiveStatusesAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.attachAdapter(adapter)
        }

        presenter.subscribe()
    }

    override fun showData(statuses: List<RenVMStatus>) {
        adapter.setItems(statuses)
    }
}