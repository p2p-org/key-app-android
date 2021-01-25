package com.p2p.wowlet.fragment.dashboard.dialog.addcoin

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.p2p.wowlet.R
import com.p2p.wowlet.databinding.DialogAddCoinBottomSheetBinding
import com.p2p.wowlet.fragment.dashboard.dialog.addcoin.adapter.AddCoinAdapter
import com.p2p.wowlet.fragment.dashboard.viewmodel.DashboardViewModel
import com.wowlet.entities.local.WalletItem
import kotlinx.android.synthetic.main.dialog_add_coin_bottom_sheet.*
import kotlinx.android.synthetic.main.item_add_coin.*
import org.koin.androidx.viewmodel.ext.android.viewModel
class AddCoinBottomSheet(
    private val goToDetailWalletFragment: (wallet: WalletItem) -> Unit,
    private val goToSolanaExplorerFragment: (mintAddress: String) -> Unit
) : BottomSheetDialogFragment() {
    private val dashboardViewModel: DashboardViewModel by viewModel()
    lateinit var binding: DialogAddCoinBottomSheetBinding

    private var addCoinPosition: Int? = null
    private var addCoinProgressBar: ProgressBar? = null
    private lateinit var addCoinAdapter: AddCoinAdapter

    companion object {
        const val TAG_ADD_COIN = "AddCoinBottomSheet"
        const val NAV_TAG_COIN_NO_ADDED_ERROR = "coin_no_added"
        fun newInstance(
            goToDetailWalletFragment: (wallet: WalletItem) -> Unit,
            goToSolanaExplorerFragment: (mintAddress: String) -> Unit
        ): AddCoinBottomSheet {
            return AddCoinBottomSheet(goToDetailWalletFragment, goToSolanaExplorerFragment)
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
        binding.viewModel = dashboardViewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addCoinAdapter = AddCoinAdapter(requireContext(), dashboardViewModel, goToSolanaExplorerFragment)
        binding.txtCloseDialog.setOnClickListener {
            dismiss()
        }
        dashboardViewModel.getAddCoinList()
        dashboardViewModel.getAddCoinData.observe(viewLifecycleOwner, {
            vRvAddCoin.apply {
                if (adapter == null) {
                    adapter = addCoinAdapter
                }
                addCoinAdapter.updateList(it)
            }

        })


        dashboardViewModel.coinIsSuccessfullyAdded.observe(viewLifecycleOwner, {
            val walletItem = WalletItem(
                tokenSymbol = it.tokenSymbol,
                mintAddress = it.mintAddress,
                tokenName = it.tokenName,
                icon = it.icon
            )
            if (it.navigatingBack) return@observe
            goToDetailWalletFragment.invoke(walletItem)
            dashboardViewModel.updateNavValue(it)
        })



        dashboardViewModel.onCoinAdd.observe(viewLifecycleOwner, { addCoinItem->
            if (addCoinItem.navigatingBack) return@observe
            addCoinAdapter.disableCallbacks()
            val itemAddCoinBinding = addCoinAdapter.getItemAddCoinBinding(addCoinItem.mintAddress)

            itemAddCoinBinding?.apply {
                txtWillCost.isVisible = false
                txtAddToken.text = getString(R.string.adding_token_to_your_wallet)
                txtErrorMessage.text = ""
                btnViewInExplorer.isEnabled = false
                lAddCoin.isEnabled = false
                binding.txtCloseDialog.isEnabled = false
                this@AddCoinBottomSheet.isCancelable = false
            }

        })

        dashboardViewModel.progressData.observe(viewLifecycleOwner, { progress->
            val position = addCoinAdapter.getExpandedItemPosition()
            if (position != addCoinPosition) {
                addCoinPosition = position
                addCoinProgressBar = addCoinAdapter.getBindingByPosition(position)?.pbAddCoin
            }
            addCoinProgressBar?.progress = progress
        })
        dashboardViewModel.coinNoAddedError.observe(viewLifecycleOwner, {
            if (it == NAV_TAG_COIN_NO_ADDED_ERROR) return@observe
            val position = addCoinAdapter.getExpandedItemPosition()
            val itemAddCoinBinding = addCoinAdapter.getBindingByPosition(position)
            itemAddCoinBinding?.apply {
                txtErrorMessage.text = getString(R.string.we_couldn_t_add_the_coin_error_message)
                txtAddToken.text = getString(R.string.add_token)
                txtWillCost.isVisible = true
                btnViewInExplorer.isEnabled = true
                lAddCoin.isEnabled = true
                binding.txtCloseDialog.isEnabled = true
                this@AddCoinBottomSheet.isCancelable = true
            }
            addCoinAdapter.enableCallbacks()
        })

    }

//    override fun onResume() {
//        super.onResume()
//        dialog?.run {
//            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        }
//    }
}