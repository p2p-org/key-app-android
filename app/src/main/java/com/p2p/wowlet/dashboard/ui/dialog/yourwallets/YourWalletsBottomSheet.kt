package com.p2p.wowlet.dashboard.ui.dialog.yourwallets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.p2p.wowlet.R
import com.p2p.wowlet.dashboard.ui.dialog.yourwallets.adapter.YourWalletsAdapter
import com.p2p.wowlet.databinding.DialogMyWalletBinding
import com.p2p.wowlet.dashboard.ui.dialog.sendcoins.viewmodel.SendCoinsViewModel
import com.p2p.wowlet.dashboard.model.local.WalletItem
import com.p2p.wowlet.utils.viewbinding.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class YourWalletsBottomSheet(
    private val title: String,
    private val itemWallet: (data: WalletItem) -> Unit
) :
    BottomSheetDialogFragment() {

    private lateinit var adapter: YourWalletsAdapter
    private val sendCoinsViewModel: SendCoinsViewModel by viewModel()

    private val binding: DialogMyWalletBinding by viewBinding()

    companion object {
        const val YOUR_WALLET = "yourWallet"
        fun newInstance(
            title: String,
            itemWallet: (data: WalletItem) -> Unit
        ): YourWalletsBottomSheet =
            YourWalletsBottomSheet(title, itemWallet)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_my_wallet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = YourWalletsAdapter(mutableListOf(), sendCoinsViewModel)
        binding.apply {
            vRvWallets.layoutManager = LinearLayoutManager(requireContext())
            vRvWallets.adapter = adapter
            yourWalletTitle.text = title
        }
        sendCoinsViewModel.getWalletItems()
        sendCoinsViewModel.getWalletData.observe(
            viewLifecycleOwner,
            Observer {
                adapter.updateList(it)
            }
        )
        sendCoinsViewModel.walletItemData.observe(
            viewLifecycleOwner,
            {
                if (it.tokenSymbol.isNotEmpty()) {
                    itemWallet.invoke(it)
                    dismiss()
                }
            }
        )
    }
}