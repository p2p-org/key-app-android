package org.p2p.wallet.striga.iban

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.uikit.components.finance_block.MainCellModel
import org.p2p.uikit.components.finance_block.mainCellDelegate
import org.p2p.uikit.databinding.FragmentStrigaIbanAccountBinding
import org.p2p.uikit.utils.attachAdapter
import org.p2p.uikit.utils.recycler.decoration.groupedRoundingMainCellDecoration
import org.p2p.wallet.R
import org.p2p.wallet.common.adapter.CommonAnyCellAdapter
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding

class StrigaUserIbanDetailsFragment :
    BaseMvpFragment<StrigaUserIbanDetailsContract.View, StrigaUserIbanDetailsContract.Presenter>(
        R.layout.fragment_striga_iban_account
    ),
    StrigaUserIbanDetailsContract.View {

    private val binding: FragmentStrigaIbanAccountBinding by viewBinding()

    private val adapter = CommonAnyCellAdapter(
        mainCellDelegate(
            inflateListener = {
            }
        )
    )

    override val presenter: StrigaUserIbanDetailsContract.Presenter by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setOnClickListener {
            popBackStack()
        }

        binding.recyclerView.attachAdapter(adapter)
        binding.recyclerView.addItemDecoration(groupedRoundingMainCellDecoration())
        binding.recyclerView.addItemDecoration(StrigaUserIbanItemDecoration(requireContext()))
    }

    override fun showIbanDetails(details: List<MainCellModel>) {
        adapter.items = details
    }
}
