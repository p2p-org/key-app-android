package org.p2p.wallet.sell.ui.information

import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpBottomSheet
import org.p2p.wallet.databinding.DialogSellInformationBinding

class SellInformationBottomSheet :
    BaseMvpBottomSheet<SellInformationContract.View, SellInformationContract.Presenter>(
        R.layout.dialog_sell_information
    ),
    SellInformationContract.View {

    companion object {
        const val SELL_INFORMATION_REQUEST_KEY = "SELL_INFORMATION_REQUEST_KEY"
        const val SELL_INFORMATION_RESULT_KEY = "SELL_INFORMATION_RESULT_KEY"

        fun newInstance(fm: FragmentManager) {
            SellInformationBottomSheet()
                .show(fm, SellInformationBottomSheet::javaClass.name)
        }
    }

    override val presenter: SellInformationContract.Presenter by inject()
    private lateinit var binding: DialogSellInformationBinding

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_Rounded

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogSellInformationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            imageViewSellInformClose.setOnClickListener { dismiss() }
            buttonSellInformConfirm.setOnClickListener { presenter.onOkClick(checkBoxSellInformNotShow.isChecked) }
        }
    }

    override fun dismissWithOkResult() {
        setFragmentResult(
            SELL_INFORMATION_REQUEST_KEY, bundleOf(SELL_INFORMATION_RESULT_KEY to true)
        )
        dismiss()
    }
}
