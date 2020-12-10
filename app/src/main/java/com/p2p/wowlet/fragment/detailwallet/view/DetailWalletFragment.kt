package com.p2p.wowlet.fragment.detailwallet.view

import androidx.recyclerview.widget.LinearLayoutManager
import com.p2p.wowlet.R
import com.p2p.wowlet.activity.MainActivity
import com.p2p.wowlet.appbase.FragmentBaseMVVM
import com.p2p.wowlet.appbase.utils.dataBinding
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewcommand.ViewCommand
import com.p2p.wowlet.databinding.FragmentDetailActivityBinding
import com.p2p.wowlet.fragment.dashboard.dialog.TransactionBottomSheet
import com.p2p.wowlet.fragment.detailwallet.adapter.ActivityAdapter
import com.p2p.wowlet.fragment.detailwallet.viewmodel.DetailWalletViewModel
import com.p2p.wowlet.utils.initChart
import org.koin.androidx.viewmodel.ext.android.viewModel


class DetailWalletFragment :
    FragmentBaseMVVM<DetailWalletViewModel, FragmentDetailActivityBinding>() {

    private lateinit var activityAdapter: ActivityAdapter
    override val viewModel: DetailWalletViewModel by viewModel()
    override val binding: FragmentDetailActivityBinding by dataBinding(R.layout.fragment_detail_activity)
    private var publicKey: String = ""
    private var icon: String = ""
    private var tokenName: String = ""

    companion object {
        const val PUBLIC_KEY = "depositAddress"
        const val ICON = "icon"
        const val TOKEN_NAME = "tokenName"
    }

    override fun initData() {
        arguments?.let {
            publicKey = it.getString(PUBLIC_KEY, "")
            icon = it.getString(ICON, "")
            tokenName = it.getString(TOKEN_NAME, "")
        }
        viewModel.getActivityList(publicKey, icon,tokenName)
    }

    override fun initView() {
        binding.run {
            viewModel = this@DetailWalletFragment.viewModel
            vTitle.text = tokenName
        }
        activityAdapter = ActivityAdapter(mutableListOf(),viewModel)
        binding.vRvActivity.adapter = activityAdapter
        binding.vRvActivity.layoutManager = LinearLayoutManager(context)
        binding.vWalletAddress.text = publicKey
    }

    override fun observes() {
        observe(viewModel.getChartData) {
            binding.vChartData.initChart(it)
        }
        observe(viewModel.getActivityData) {
            activityAdapter.updateList(it)
        }
    }

    override fun processViewCommand(command: ViewCommand) {
        when (command) {
            is Command.NavigateUpViewCommand -> {
                navigateFragment(command.destinationId)
                (activity as MainActivity).showHideNav(true)
            }
            is Command.NavigateScannerViewCommand -> {
                navigateFragment(command.destinationId)
                (activity as MainActivity).showHideNav(false)
            }
            is Command.OpenTransactionDialogViewCommand -> {
                TransactionBottomSheet.newInstance(command.itemActivity){destinationId, bundle ->
                    navigateFragment(destinationId,bundle)
                }.show(childFragmentManager,TransactionBottomSheet.TRANSACTION_DIALOG)
                (activity as MainActivity).showHideNav(false)
            }
        }
    }

    override fun navigateUp() {
        viewModel.navigateUp()
    }

}