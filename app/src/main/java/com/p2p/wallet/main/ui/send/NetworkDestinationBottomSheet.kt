package com.p2p.wallet.main.ui.send

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.p2p.wallet.R
import com.p2p.wallet.databinding.DialogNetworkDestinationBinding
import com.p2p.wallet.main.model.NetworkType
import com.p2p.wallet.utils.viewbinding.viewBinding

class NetworkDestinationBottomSheet(
    private val onNetworkSelected: (NetworkType) -> Unit
) : BottomSheetDialogFragment() {

    companion object {
        fun show(fm: FragmentManager, onNetworkSelected: (NetworkType) -> Unit) =
            NetworkDestinationBottomSheet(onNetworkSelected).show(fm, FeeInfoBottomSheet::javaClass.name)
    }

    private val binding: DialogNetworkDestinationBinding by viewBinding()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_network_destination, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.solanaView.setOnClickListener {
            onNetworkSelected.invoke(NetworkType.SOLANA)
            dismissAllowingStateLoss()
        }

        binding.bitcoinView.setOnClickListener {
            onNetworkSelected.invoke(NetworkType.BITCOIN)
            dismissAllowingStateLoss()
        }
    }

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_Rounded
}