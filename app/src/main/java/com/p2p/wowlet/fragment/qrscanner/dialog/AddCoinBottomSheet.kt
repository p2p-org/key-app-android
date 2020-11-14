package com.p2p.wowlet.fragment.qrscanner.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.p2p.wowlet.R
import com.p2p.wowlet.databinding.DialogAddCoinBottomSheetBinding
import com.p2p.wowlet.fragment.qrscanner.viewmodel.QrScannerViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_add_coin_bottom_sheet.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddCoinBottomSheet : BottomSheetDialogFragment() {
    private val qrScannerViewModel: QrScannerViewModel by viewModel()
    lateinit var binding: DialogAddCoinBottomSheetBinding

    companion object {
        const val TAG_ADD_COIN = "AddCoinBottomSheet"
        fun newInstance(): AddCoinBottomSheet {
            return AddCoinBottomSheet()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.dialog_add_coin_bottom_sheet, container, false
        )
        binding.viewModel = qrScannerViewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        qrScannerViewModel.getAddCoinList()
        vClose.setOnClickListener {
            dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        dialog?.run {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }
}