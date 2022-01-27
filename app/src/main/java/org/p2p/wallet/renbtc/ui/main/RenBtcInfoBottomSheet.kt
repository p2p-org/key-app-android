package org.p2p.wallet.renbtc.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import org.p2p.wallet.R
import org.p2p.wallet.common.ui.NonDraggableBottomSheetDialogFragment
import org.p2p.wallet.databinding.DialogBtcNetworkInfoBinding
import org.p2p.wallet.utils.viewbinding.viewBinding

class RenBtcInfoBottomSheet(private val block: () -> Unit) : NonDraggableBottomSheetDialogFragment() {

    companion object {
        fun show(fm: FragmentManager, block: () -> Unit) =
            RenBtcInfoBottomSheet(block).show(fm, RenBtcInfoBottomSheet::javaClass.name)
    }

    private val binding: DialogBtcNetworkInfoBinding by viewBinding()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_btc_network_info, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            progressButton.setOnClickListener {
                block.invoke()
                dismissAllowingStateLoss()
            }
        }
    }
}