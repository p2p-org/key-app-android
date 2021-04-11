package com.p2p.wowlet.dashboard.dialog.swap.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.p2p.wowlet.R
import com.p2p.wowlet.backupwallat.secretkeys.utils.hideSoftKeyboard
import com.p2p.wowlet.dashboard.dialog.swap.adapter.SelectTokenToSwapAdapter
import com.p2p.wowlet.dashboard.dialog.swap.viewmodel.SwapViewModel
import com.p2p.wowlet.databinding.BottomSheetSelectTokenToSwapBinding
import com.p2p.wowlet.entities.local.WalletItem
import com.p2p.wowlet.utils.popBackStack
import com.p2p.wowlet.utils.viewbinding.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

// R.layout.bottom_sheet_select_token_to_swap
class SelectTokenToSwapBottomSheet(
    private val selectedTokenFrom: WalletItem,
    private val allMyWallets: List<WalletItem>,
    private val selectTokenToSwap: (walletItem: WalletItem) -> Unit
) : BottomSheetDialogFragment() {

    private val swapViewModel: SwapViewModel by viewModel()

    private val binding: BottomSheetSelectTokenToSwapBinding by viewBinding()

    companion object {
        const val TAG_SELECT_TOKEN_TO_SWAP = "SelectTokenToSwap"
        fun newInstance(
            selectedTokenFrom: WalletItem,
            allMyWallets: List<WalletItem>,
            selectTokenToSwap: (walletItem: WalletItem) -> Unit
        ): SelectTokenToSwapBottomSheet {
            return SelectTokenToSwapBottomSheet(selectedTokenFrom, allMyWallets, selectTokenToSwap)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.bottom_sheet_select_token_to_swap, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            rvSwapDestinationTokens.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = SelectTokenToSwapAdapter(selectedTokenFrom).apply {
                    initList(allMyWallets)
                    setSearchBarEditText(edtSearchBar)
                    setOnItemClickListener { selectedWalletItem ->
                        selectTokenToSwap.invoke(selectedWalletItem)
                        dismiss()
                    }
                }
            }

            txtClose.setOnClickListener { popBackStack() }
            clearImageView.setOnClickListener { swapViewModel.clearSearchBar() }
        }

        initObserves()
    }

    override fun onStart() {
        super.onStart()
//        dialog?.makeFullScreen(binding.root, this)
    }

    private fun initObserves() {
        swapViewModel.clearSearchBar.observe(viewLifecycleOwner) {
            binding.edtSearchBar.apply {
                setText("")
                clearFocus()
                hideSoftKeyboard()
            }
        }

        swapViewModel.isCloseIconVisible.observe(viewLifecycleOwner) {
            binding.clearImageView.isVisible = it
        }
    }
}