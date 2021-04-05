package com.p2p.wowlet.fragment.dashboard.dialog.sendyourwallet

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.p2p.wowlet.R
import com.p2p.wowlet.databinding.DialogSendYourWalletBinding
import com.p2p.wowlet.fragment.dashboard.dialog.detailwallet.viewmodel.DetailWalletViewModel
import com.p2p.wowlet.utils.bindadapter.imageSource
import com.p2p.wowlet.utils.bindadapter.imageSourceBitmap
import com.p2p.wowlet.utils.shareText
import com.p2p.wowlet.utils.viewbinding.viewBinding
import com.wowlet.entities.local.EnterWallet
import org.koin.androidx.viewmodel.ext.android.viewModel

class YourWalletBottomSheet(private val enterWallet: EnterWallet) : BottomSheetDialogFragment() {

    companion object {
        const val ENTER_YOUR_WALLET = "EnterYourWallet"
        fun newInstance(enterWallet: EnterWallet): YourWalletBottomSheet {
            return YourWalletBottomSheet(enterWallet)
        }
    }

    private val dashboardViewModel: DetailWalletViewModel by viewModel()

    private val binding: DialogSendYourWalletBinding by viewBinding()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_send_your_wallet, container, false)

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.dismissDialog.setOnClickListener { dismiss() }
        binding.shareWalletContainer.setOnClickListener {
            context?.run { shareText(enterWallet.walletAddress) }
        }
        binding.enterYouWalletTitle.text = getString(R.string.send_to_your_wallet, enterWallet.name)
        binding.walletQrCodeIv.imageSourceBitmap(enterWallet.qrCode)
        binding.iconImageView.imageSource(enterWallet.icon)
        binding.walletAddressTextView.text = enterWallet.name
        binding.tokenText.text = (enterWallet.walletAddress)
    }

    override fun onResume() {
        super.onResume()
        dialog?.run {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            isCancelable = false
        }
    }
}