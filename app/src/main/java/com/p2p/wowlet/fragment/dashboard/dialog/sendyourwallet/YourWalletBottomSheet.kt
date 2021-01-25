package com.p2p.wowlet.fragment.dashboard.dialog.sendyourwallet

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.p2p.wowlet.R
import com.p2p.wowlet.databinding.DialogSendYourWalletBinding
import com.p2p.wowlet.fragment.dashboard.dialog.detailwallet.viewmodel.DetailWalletViewModel
import com.p2p.wowlet.utils.shareText
import com.wowlet.entities.local.EnterWallet
import org.koin.androidx.viewmodel.ext.android.viewModel

class YourWalletBottomSheet(private val enterWallet: EnterWallet) : BottomSheetDialogFragment() {

    private val dashboardViewModel: DetailWalletViewModel by viewModel()
    lateinit var binding: DialogSendYourWalletBinding

    companion object {
        const val ENTER_YOUR_WALLET = "EnterYourWallet"
        fun newInstance(enterWallet: EnterWallet): YourWalletBottomSheet {
            return YourWalletBottomSheet(enterWallet)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.dialog_send_your_wallet, container, false
        )
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.dismissDialog.setOnClickListener { dismiss() }
        binding.viewModel = dashboardViewModel
        binding.itemModel = enterWallet
        binding.shareWalletContainer.setOnClickListener {
            context?.run { shareText(enterWallet.walletAddress) }
        }
    }

    override fun onResume() {
        super.onResume()
        dialog?.run {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            isCancelable = false
        }
    }
}