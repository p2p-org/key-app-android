package org.p2p.wallet.send.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.p2p.wallet.R
import org.p2p.wallet.databinding.DialogFeeInfoBinding
import org.p2p.wallet.utils.viewbinding.viewBinding

class FeeInfoBottomSheet : BottomSheetDialogFragment() {

    companion object {
        fun show(fm: FragmentManager) = FeeInfoBottomSheet().show(fm, FeeInfoBottomSheet::javaClass.name)
    }

    private val binding: DialogFeeInfoBinding by viewBinding()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_fee_info, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.doneButton.setOnClickListener {
            dismissAllowingStateLoss()
        }
    }

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_Rounded
}