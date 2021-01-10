package com.p2p.wowlet.fragment.dashboard.dialog.addcoin

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.p2p.wowlet.R
import com.p2p.wowlet.activity.MainActivity
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.databinding.DialogAddCoinBottomSheetBinding
import com.p2p.wowlet.fragment.dashboard.dialog.addcoin.adapter.AddCoinAdapter
import com.p2p.wowlet.fragment.dashboard.view.DashboardFragment
import com.p2p.wowlet.fragment.dashboard.viewmodel.DashboardViewModel
import com.wowlet.entities.local.WalletItem
import kotlinx.android.synthetic.main.dialog_add_coin_bottom_sheet.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddCoinBottomSheet : BottomSheetDialogFragment() {
    private val dashboardViewModel: DashboardViewModel by viewModel()
    lateinit var binding: DialogAddCoinBottomSheetBinding

    private lateinit var addCoinAdapter: AddCoinAdapter

    companion object {
        const val TAG_ADD_COIN = "AddCoinBottomSheet"
        fun newInstance(): AddCoinBottomSheet {
            return AddCoinBottomSheet()
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
        addCoinAdapter = AddCoinAdapter(dashboardViewModel,viewLifecycleOwner)
        binding.txtCloseDialog.setOnClickListener {
            dismiss()
        }
        dashboardViewModel.getAddCoinList()
        dashboardViewModel.getAddCoinData.observe(viewLifecycleOwner, {
            vRvAddCoin.apply {
                if (adapter == null) {
                    adapter = addCoinAdapter
                    this.layoutManager = LinearLayoutManager(this.context)
                }
                addCoinAdapter.updateList(it)
            }

        })
//        dashboardViewModel.getMinimumBalanceData.observe(viewLifecycleOwner, {
//            info.text = String.format(getString(R.string.add_coin_token_info), it)
//        })

        dashboardViewModel.coinIsSuccessfullyAdded.observe(viewLifecycleOwner, {
            val walletItem = WalletItem(
                tokenSymbol = it.tokenSymbol,
                mintAddress = it.mintAddress,
                tokenName = it.tokenName,
                icon = it.icon
            )
            dashboardViewModel.goToDetailWalletFragment(walletItem)
        })

        dashboardViewModel.coinNoAddedError.observe(viewLifecycleOwner, {
            //Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        })

        dashboardViewModel.command.observe(viewLifecycleOwner, {
            when(it) {
                is Command.NavigateWalletViewCommand -> {
                    (parentFragment as DashboardFragment).findNavController().navigate(it.destinationId)
                    (activity as MainActivity).showHideNav(false)
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        dialog?.run {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }
}