package com.p2p.wowlet.fragment.dashboard.dialog.addcoin

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
import com.p2p.wowlet.databinding.DialogAddCoinBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.p2p.wowlet.fragment.dashboard.dialog.addcoin.adapter.AddCoinAdapter
import com.p2p.wowlet.fragment.dashboard.viewmodel.DashboardViewModel
import kotlinx.android.synthetic.main.dialog_add_coin_bottom_sheet.*
import org.bouncycastle.util.Strings
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddCoinBottomSheet : BottomSheetDialogFragment() {
    private val dashboardViewModel: DashboardViewModel by viewModel()
    lateinit var binding: DialogAddCoinBottomSheetBinding

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
        binding.dismissDialog.setOnClickListener {
            dismiss()
        }
        dashboardViewModel.getAddCoinList()
        dashboardViewModel.getAddCoinData.observe(viewLifecycleOwner, Observer {
            vRvAddCoin.apply {
                adapter = AddCoinAdapter(it, dashboardViewModel)
                this.layoutManager = LinearLayoutManager(this.context)
            }

        })
        dashboardViewModel.getMinimumBalanceData.observe(viewLifecycleOwner, Observer {
            info.text= String.format(getString(R.string.add_coin_token_info),it)
        })

    }

    override fun onResume() {
        super.onResume()
        dialog?.run {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }
}