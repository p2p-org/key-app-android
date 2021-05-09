package com.p2p.wallet.dashboard.ui.dialog.allmytokens

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.p2p.wallet.R
import com.p2p.wallet.dashboard.ui.dialog.allmytokens.adapter.WalletsAdapter
import com.p2p.wallet.dashboard.ui.viewmodel.DashboardViewModel
import com.p2p.wallet.databinding.DialogAllMyTokensBinding
import com.p2p.wallet.token.model.Token
import com.p2p.wallet.dashboard.model.local.YourWallets
import com.p2p.wallet.utils.viewbinding.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class AllMyTokensBottomSheet(
    private var yourWallets: YourWallets,
    private val addCoinClickListener: ((updateAllMyTokens: () -> Unit) -> Unit),
    private val itemClickListener: ((Token) -> Unit)
) : BottomSheetDialogFragment() {

    private val dashboardViewModel: DashboardViewModel by viewModel()
    private lateinit var walletsAdapter: WalletsAdapter

    companion object {
        const val TAG_ALL_MY_TOKENS_DIALOG = "AllMyTokensDialog"
        fun newInstance(
            yourWallets: YourWallets,
            addCoinClickListener: ((updateAllMyTokens: () -> Unit) -> Unit),
            itemClickListener: ((Token) -> Unit)
        ): AllMyTokensBottomSheet =
            AllMyTokensBottomSheet(yourWallets, addCoinClickListener, itemClickListener)
    }

    private val binding: DialogAllMyTokensBinding by viewBinding()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_all_my_tokens, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dashboardViewModel.getAllWallets()
        binding.vRvWallets.layoutManager = LinearLayoutManager(requireContext())
        walletsAdapter = WalletsAdapter(dashboardViewModel, mutableListOf()) {
            itemClickListener.invoke(it)
        }
        binding.vRvWallets.adapter = walletsAdapter
        binding.addCoinBtn.setOnClickListener {
            addCoinClickListener.invoke {
                dashboardViewModel.clearGetWalletData()
                dashboardViewModel.getAllWallets()
            }
        }
//        binding.vPieChartData.drawChart(yourWallets.pieChartList)
        binding.vPrice.text = String.format(getString(R.string.usd_symbol_2), yourWallets.balance)
        dashboardViewModel.getWalletData.observe(
            viewLifecycleOwner,
            {
                walletsAdapter.updateData(it)
            }
        )
    }

    override fun onResume() {
        super.onResume()
        dialog?.run {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }
}