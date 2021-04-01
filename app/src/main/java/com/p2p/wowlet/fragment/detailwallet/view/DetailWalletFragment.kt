package com.p2p.wowlet.fragment.detailwallet.view

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.FragmentBaseMVVM
import com.p2p.wowlet.appbase.utils.dataBinding
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewcommand.ViewCommand
import com.p2p.wowlet.databinding.FragmentDetailActivityBinding
import com.p2p.wowlet.dialog.sendcoins.view.SendCoinsBottomSheet
import com.p2p.wowlet.dialog.sendcoins.view.SendCoinsBottomSheet.Companion.TAG_SEND_COIN
import com.p2p.wowlet.fragment.blockchainexplorer.view.BlockChainExplorerFragment
import com.p2p.wowlet.fragment.dashboard.dialog.TransactionBottomSheet
import com.p2p.wowlet.fragment.detailwallet.adapter.ActivityAdapter
import com.p2p.wowlet.fragment.detailwallet.dialog.YourWalletBottomSheet
import com.p2p.wowlet.fragment.detailwallet.viewmodel.DetailWalletViewModel
import com.p2p.wowlet.utils.*
import com.wowlet.entities.local.WalletItem
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class DetailWalletFragment :
    FragmentBaseMVVM<DetailWalletViewModel, FragmentDetailActivityBinding>() {

    private lateinit var activityAdapter: ActivityAdapter
    override val viewModel: DetailWalletViewModel by viewModel()
    override val binding: FragmentDetailActivityBinding by dataBinding(R.layout.fragment_detail_activity)
    private var walletItem: WalletItem? = null
    private var selectedTextView: AppCompatTextView? = null
    private val cal = Calendar.getInstance()

    companion object {
        const val WALLET_ITEM = "walletItem"
    }

    override fun initData() {
        arguments?.let {
            walletItem = it.getParcelable(WALLET_ITEM)
            println("debug: icon = ${walletItem?.icon}")
        }
        walletItem?.run {
            viewModel.getActivityList(this.depositAddress, icon, tokenName, this.tokenSymbol)
            viewModel.getChartData(this.tokenSymbol)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun initView() {
        binding.run {
            viewModel = this@DetailWalletFragment.viewModel
            Glide.with(this@DetailWalletFragment)
                .load(walletItem?.icon)
                .apply(RequestOptions.bitmapTransform(RoundedCorners(38)))
                .into(currencyIcon)
            vTitle.text = walletItem?.tokenName
            vWalletAddress.text = walletItem?.depositAddress
            vPrice.text = "$${walletItem?.price?.roundCurrencyValue()}"
            vTokenValue.text = "${walletItem?.amount} ${walletItem?.tokenSymbol}"
            vWalletAddress.setOnClickListener {
                context?.run { copyClipboard(vWalletAddress.text.toString()) }
            }
            getChartByDay.setOnClickListener {
                getChartByDay.changeTextColor(selectedTextView)
                val yesterday = getYesterday()
                walletItem?.tokenSymbol?.run {
                    this@DetailWalletFragment.viewModel.getChartDataByDate(
                        this,
                        yesterday,
                        cal.timeInMillis
                    )
                }
                selectedTextView = getChartByDay
            }
            getChartByWeek.setOnClickListener {
                getChartByWeek.changeTextColor(selectedTextView)
                val weekly = getWeekly()
                walletItem?.tokenSymbol?.run {
                    this@DetailWalletFragment.viewModel.getChartDataByDate(
                        this,
                        weekly,
                        cal.timeInMillis
                    )
                }
                selectedTextView = getChartByWeek
            }
            getChartByMonth.setOnClickListener {
                getChartByMonth.changeTextColor(selectedTextView)
                val monthly = getMonthly()
                walletItem?.tokenSymbol?.run {
                    this@DetailWalletFragment.viewModel.getChartDataByDate(
                        this,
                        monthly,
                        cal.timeInMillis
                    )
                }
                selectedTextView = getChartByMonth
            }
            getChartByYear.setOnClickListener {
                getChartByYear.changeTextColor(selectedTextView)
                val year = getOneHour()
                walletItem?.tokenSymbol?.run {
                    this@DetailWalletFragment.viewModel.getChartDataByDate(
                        this,
                        year,
                        cal.timeInMillis
                    )
                }
                selectedTextView = getChartByYear
            }
            getChartByAll.setOnClickListener {
                getChartByAll.changeTextColor(selectedTextView)
                walletItem?.tokenSymbol?.run {
                    this@DetailWalletFragment.viewModel.getChartData(this)
                }
                selectedTextView = getChartByAll
            }
//            sendCoinAddress.setOnClickListener {
//                walletItem?.let { this@DetailWalletFragment.viewModel.goToSendCoin(it) }
//            }
            vQrScanner.setOnClickListener {
                walletItem?.let { item ->
                    this@DetailWalletFragment.viewModel.goToQrScanner(item)
                }
            }
        }
        activityAdapter = ActivityAdapter(mutableListOf(), viewModel)
        binding.vRvActivity.adapter = activityAdapter
        binding.vRvActivity.layoutManager = LinearLayoutManager(context)
    }

    override fun observes() {
        observe(viewModel.getChartData) {
            binding.vChartData.initChart(it)
        }
        observe(viewModel.getActivityData) {
            activityAdapter.updateList(it)
        }

        observe(viewModel.getActivityDataError) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    override fun processViewCommand(command: ViewCommand) {
        when (command) {
            is Command.NavigateUpBackStackCommand -> {
                popBackStack()
            }
            is Command.OpenSendCoinDialogViewCommand -> {
                SendCoinsBottomSheet.newInstance(
                    command.walletItem,
                    command.walletAddress,
                ).show(
                    childFragmentManager,
                    TAG_SEND_COIN
                )
            }
            is Command.OpenSwapBottomSheetViewCommand -> {
//                navigateFragment(command.destinationId)
//                (activity as MainActivity).showHideNav(false)
            }
            is Command.OpenTransactionDialogViewCommand -> {
                TransactionBottomSheet.newInstance(command.itemActivity) {
                    replace(BlockChainExplorerFragment.createScreen(it))
                }.show(childFragmentManager, TransactionBottomSheet.TRANSACTION_DIALOG)
            }
            is Command.YourWalletDialogViewCommand -> {
                YourWalletBottomSheet.newInstance(command.enterWallet).show(
                    childFragmentManager,
                    YourWalletBottomSheet.ENTER_YOUR_WALLET
                )
            }
        }
    }
}