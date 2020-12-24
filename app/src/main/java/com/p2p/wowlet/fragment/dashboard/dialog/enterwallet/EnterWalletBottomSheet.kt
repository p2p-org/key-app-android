package com.p2p.wowlet.fragment.dashboard.dialog.enterwallet

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.p2p.wowlet.R
import com.p2p.wowlet.databinding.DialogEnterWalletBinding
import com.p2p.wowlet.fragment.dashboard.adapter.EnterWalletPagerAdapter
import com.p2p.wowlet.fragment.dashboard.viewmodel.DashboardViewModel
import com.wowlet.entities.local.EnterWallet
import org.koin.androidx.viewmodel.ext.android.viewModel


class EnterWalletBottomSheet(val list: List<EnterWallet>) : BottomSheetDialogFragment() {

    private lateinit var adapter: EnterWalletPagerAdapter
    private val dashboardViewModel: DashboardViewModel by viewModel()
    lateinit var binding: DialogEnterWalletBinding

    companion object {
        const val ENTER_WALLET = "EnterWallet"
        fun newInstance(list: List<EnterWallet>): EnterWalletBottomSheet {
            return EnterWalletBottomSheet(list)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.dialog_enter_wallet, container, false
        )

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = dashboardViewModel
        adapter = EnterWalletPagerAdapter(list)
        binding.viewPager.adapter = adapter

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val enterWallet = list[position]
                binding.enterWalletTitle.text = enterWallet.name
            }
        })
        binding.pageIndicator.setViewPager(binding.viewPager)
        adapter.registerAdapterDataObserver(binding.pageIndicator.adapterDataObserver)
    }

    override fun onResume() {
        super.onResume()
        dialog?.run {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }
}