package com.p2p.wowlet.fragment.sendcoins.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.p2p.wowlet.R
import com.p2p.wowlet.databinding.DialogMyWalletBinding
import com.p2p.wowlet.fragment.sendcoins.viewmodel.SendCoinsViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.p2p.wowlet.fragment.sendcoins.adapter.YourWalletsAdapter
import kotlinx.android.synthetic.main.dialog_my_wallet.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class YourWalletsBottomSheet : BottomSheetDialogFragment() {

    private val sendCoinsViewModel: SendCoinsViewModel by viewModel()
    private lateinit var binding: DialogMyWalletBinding

    companion object {
        const val YOUR_WALLET="yourWallet"
        fun newInstance(): YourWalletsBottomSheet = YourWalletsBottomSheet()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.dialog_my_wallet, container, false
        )
        binding.viewModel = sendCoinsViewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sendCoinsViewModel.getWalletItems()
        sendCoinsViewModel.getWalletData.observe(viewLifecycleOwner, Observer {
            vRvWallets.apply {
                adapter = YourWalletsAdapter(it)
                this.layoutManager = LinearLayoutManager(this.context)
            }

        })
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