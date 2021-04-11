package com.p2p.wowlet.dashboard.dialog.addcoin

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.p2p.wowlet.R
import com.p2p.wowlet.dashboard.dialog.addcoin.adapter.AddCoinAdapter
import com.p2p.wowlet.dashboard.viewmodel.DashboardViewModel
import com.p2p.wowlet.databinding.DialogAddCoinBottomSheetBinding
import com.p2p.wowlet.entities.local.AddCoinItem
import com.p2p.wowlet.entities.local.WalletItem
import com.p2p.wowlet.utils.viewbinding.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale
import kotlin.collections.ArrayList

class AddCoinBottomSheet(
    private val goToDetailWalletFragment: (wallet: WalletItem) -> Unit,
    private val goToSolanaExplorerFragment: (mintAddress: String) -> Unit,
    private val updateListInAllMyTokens: () -> Unit
) : BottomSheetDialogFragment() {
    private val dashboardViewModel: DashboardViewModel by viewModel()
    private val binding: DialogAddCoinBottomSheetBinding by viewBinding()

    private var addCoinPosition: Int? = null
    private var addCoinProgressBar: ProgressBar? = null
    private lateinit var addCoinAdapter: AddCoinAdapter

    companion object {
        const val TAG_ADD_COIN = "AddCoinBottomSheet"
        const val NAV_TAG_COIN_NO_ADDED_ERROR = "coin_no_added"
        fun newInstance(
            goToDetailWalletFragment: (wallet: WalletItem) -> Unit,
            goToSolanaExplorerFragment: (mintAddress: String) -> Unit,
            updateListInAllMyTokens: () -> Unit = {}
        ): AddCoinBottomSheet {
            return AddCoinBottomSheet(goToDetailWalletFragment, goToSolanaExplorerFragment, updateListInAllMyTokens)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_add_coin_bottom_sheet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.txtCloseDialog.setOnClickListener {
            dismiss()
        }
        binding.vRvAddCoin.layoutManager = LinearLayoutManager(requireContext())

        addCoinAdapter = AddCoinAdapter(requireContext(), dashboardViewModel, goToSolanaExplorerFragment)
        dashboardViewModel.getAddCoinList()
        dashboardViewModel.getAddCoinData.observe(
            viewLifecycleOwner,
            {
                binding.vRvAddCoin.apply {
                    if (adapter == null) {
                        adapter = addCoinAdapter
                    }
                    addCoinAdapter.updateList(it)
                }
            }
        )

        binding.etSearch.doOnTextChanged { text, start, before, count ->
            dashboardViewModel.getAddCoinData.value?.let { addCoinItems ->
                if (text.toString().isNotEmpty()) {
                    val filteredList = ArrayList<AddCoinItem>()
                    addCoinItems.forEach {
                        if (it.tokenName.toLowerCase(Locale.ROOT)
                            .contains(text.toString().toLowerCase(Locale.ROOT))
                        ) {
                            filteredList.add(it)
                        }
                    }
                    addCoinAdapter.updateList(filteredList)
                } else {
                    addCoinAdapter.updateList(addCoinItems)
                }
            }
        }

        dashboardViewModel.coinIsSuccessfullyAdded.observe(
            viewLifecycleOwner,
            {
                val walletItem = it.walletAddress?.let { walletAddress ->
                    WalletItem(
                        tokenSymbol = it.tokenSymbol,
                        mintAddress = it.mintAddress,
                        tokenName = it.tokenName,
                        icon = it.icon,
                        depositAddress = walletAddress
                    )
                }
                if (it.navigatingBack) return@observe
                it.isAlreadyAdded = true
                dashboardViewModel.getAddCoinData.value?.let { addCoinItems -> addCoinAdapter.updateList(addCoinItems) }
                addCoinAdapter.enableCallbacks()
                binding.txtCloseDialog.isEnabled = true
                this@AddCoinBottomSheet.isCancelable = true
                binding.etSearch.isEnabled = true
                updateListInAllMyTokens.invoke()
                walletItem?.let { item -> goToDetailWalletFragment.invoke(item) }
                dashboardViewModel.updateNavValue(it)
            }
        )

        dashboardViewModel.onCoinAdd.observe(
            viewLifecycleOwner,
            { addCoinItem ->
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
                    binding.etSearch.isEnabled = false
                }
            }
        )

        dashboardViewModel.progressData.observe(
            viewLifecycleOwner,
            { progress ->
                val position = addCoinAdapter.getExpandedItemPosition()
                if (position != addCoinPosition) {
                    addCoinPosition = position
                    addCoinProgressBar = addCoinAdapter.getBindingByPosition(position)?.pbAddCoin
                }
                addCoinProgressBar?.progress = progress
            }
        )
        dashboardViewModel.coinNoAddedError.observe(
            viewLifecycleOwner,
            {
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
                    binding.etSearch.isEnabled = true
                }
                addCoinAdapter.enableCallbacks()
            }
        )
    }

    override fun onStart() {
        super.onStart()
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            state = BottomSheetBehavior.STATE_HALF_EXPANDED
            halfExpandedRatio = 0.99f
            binding.vRvAddCoin.minimumHeight = Resources.getSystem().displayMetrics.heightPixels
        }
    }
}