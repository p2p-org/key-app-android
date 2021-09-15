package com.p2p.wallet.main.ui.receive.solana

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.p2p.wallet.R
import com.p2p.wallet.common.ui.NonDraggableBottomSheetDialogFragment
import com.p2p.wallet.databinding.DialogReceiveInfoBinding
import com.p2p.wallet.utils.viewbinding.viewBinding

class ReceiveSolanaInfoBottomSheetDialog : NonDraggableBottomSheetDialogFragment() {

    companion object {
        fun show(fm: FragmentManager) {
            ReceiveSolanaInfoBottomSheetDialog()
                .show(fm, ReceiveSolanaInfoBottomSheetDialog::javaClass.name)
        }
    }

    private val binding: DialogReceiveInfoBinding by viewBinding()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_receive_info, container, false)

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_Rounded
}