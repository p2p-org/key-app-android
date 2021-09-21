package com.p2p.wallet.renBTC.statuses

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.p2p.wallet.R
import com.p2p.wallet.common.mvp.BaseMvpFragment
import com.p2p.wallet.databinding.FragmentReceivingStatusesBinding
import com.p2p.wallet.renBTC.model.RenVMStatus
import com.p2p.wallet.utils.attachAdapter
import com.p2p.wallet.utils.popBackStack
import com.p2p.wallet.utils.viewbinding.viewBinding
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