package org.p2p.wallet.striga.onramp.iban

import androidx.core.net.toUri
import android.content.Intent
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.uikit.components.finance_block.mainCellDelegate
import org.p2p.uikit.databinding.FragmentStrigaIbanAccountBinding
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.attachAdapter
import org.p2p.uikit.utils.recycler.decoration.groupedRoundingMainCellDecoration
import org.p2p.wallet.R
import org.p2p.wallet.common.adapter.CommonAnyCellAdapter
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.striga.onramp.iban.adapter.StrigaUserIbanItemDecoration
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding

class StrigaUserIbanDetailsFragment :
    BaseMvpFragment<StrigaUserIbanDetailsContract.View, StrigaUserIbanDetailsContract.Presenter>(
        R.layout.fragment_striga_iban_account
    ),
    StrigaUserIbanDetailsContract.View {

    companion object {
        fun create(): StrigaUserIbanDetailsFragment = StrigaUserIbanDetailsFragment()
    }

    private val binding: FragmentStrigaIbanAccountBinding by viewBinding()

    private val adapter = CommonAnyCellAdapter(
        mainCellDelegate(
            inflateListener = {
                it.setOnClickAction { _, item -> (item.payload as? String)?.also(::onCopyIconClicked) }
            }
        )
    )

    override val presenter: StrigaUserIbanDetailsContract.Presenter by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setOnClickListener { navigateBack() }

            recyclerViewIbanDetails.attachAdapter(adapter)
            recyclerViewIbanDetails.addItemDecoration(groupedRoundingMainCellDecoration())
            recyclerViewIbanDetails.addItemDecoration(StrigaUserIbanItemDecoration(requireContext()))

            informerSecond.infoLineClickListener = {
                Intent(Intent.ACTION_VIEW, getString(R.string.striga_powered_by_url).toUri())
                    .also { startActivity(it) }
            }
        }
    }

    override fun showIbanDetails(details: List<AnyCellItem>) {
        adapter.items = details
    }

    private fun onCopyIconClicked(value: String) {
        requireContext().copyToClipBoard(value)
        showUiKitSnackBar(messageResId = R.string.general_copied)
    }

    override fun navigateBack() {
        popBackStack()
    }
}
