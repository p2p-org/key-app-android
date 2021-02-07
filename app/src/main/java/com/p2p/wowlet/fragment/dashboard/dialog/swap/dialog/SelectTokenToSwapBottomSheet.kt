package com.p2p.wowlet.fragment.dashboard.dialog.swap.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewcommand.ViewCommand
import com.p2p.wowlet.databinding.BottomSheetSelectTokenToSwapBinding
import com.p2p.wowlet.dialog.utils.makeFullScreen
import com.p2p.wowlet.fragment.backupwallat.secretkeys.utils.hideSoftKeyboard
import com.p2p.wowlet.fragment.dashboard.dialog.swap.adapter.SelectTokenToSwapAdapter
import com.p2p.wowlet.fragment.dashboard.dialog.swap.viewmodel.SwapViewModel
import com.wowlet.entities.local.WalletItem
import org.koin.androidx.viewmodel.ext.android.viewModel

// R.layout.bottom_sheet_select_token_to_swap
class SelectTokenToSwapBottomSheet(
    private val selectedTokenFrom: WalletItem,
    private val allMyWallets: List<WalletItem>,
    private val selectTokenToSwap: (walletItem: WalletItem) -> Unit
) : BottomSheetDialogFragment() {

    private val swapViewModel: SwapViewModel by viewModel()

    private var _binding: BottomSheetSelectTokenToSwapBinding? = null
    private val binding: BottomSheetSelectTokenToSwapBinding get() = _binding!!

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


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_select_token_to_swap, container, false)
        initObserves()
        binding.run {
            swapViewModel = this@SelectTokenToSwapBottomSheet.swapViewModel
            lifecycleOwner = this@SelectTokenToSwapBottomSheet
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
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.makeFullScreen(binding.root, this)
    }

    private fun initObserves() {
        swapViewModel.command.observe(viewLifecycleOwner) { processViewCommands(it) }
        swapViewModel.clearSearchBar.observe(viewLifecycleOwner) {
            binding.edtSearchBar.apply {
                setText("")
                clearFocus()
                hideSoftKeyboard()
            }

        }
    }

    private fun processViewCommands(command: ViewCommand) {
        when(command) {
            is Command.NavigateUpBackStackCommand -> {
                dismiss()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}