package com.p2p.wowlet.dashboard.ui.dialog.detailwallet

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.p2p.wowlet.R
import com.p2p.wowlet.dashboard.ui.dialog.TransactionBottomSheet
import com.p2p.wowlet.dashboard.ui.dialog.detailwallet.adapter.ActivityAdapter
import com.p2p.wowlet.dashboard.ui.dialog.detailwallet.util.DividerItemDecoration
import com.p2p.wowlet.dashboard.ui.dialog.detailwallet.viewmodel.DetailWalletViewModel
import com.p2p.wowlet.databinding.DialogDetailActivityBinding
import com.p2p.wowlet.deprecated.viewcommand.Command
import com.p2p.wowlet.dashboard.model.local.WalletItem
import com.p2p.wowlet.utils.bindadapter.imageSource
import com.p2p.wowlet.utils.changeTextColor
import com.p2p.wowlet.utils.copyClipboard
import com.p2p.wowlet.utils.getFourHour
import com.p2p.wowlet.utils.getMonthly
import com.p2p.wowlet.utils.getOneHour
import com.p2p.wowlet.utils.getWeekly
import com.p2p.wowlet.utils.getYesterday
import com.p2p.wowlet.utils.initChart
import com.p2p.wowlet.utils.roundCurrencyValue
import com.p2p.wowlet.utils.viewbinding.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Calendar

class DetailWalletBottomSheet(
    private val walletItem: WalletItem,
    private val openQRScanner: ((WalletItem) -> Unit),
    private val openAddCoin: (() -> Unit),
    private val openSendCoin: ((WalletItem) -> Unit),
    private val openReceveCoin: (() -> Unit),
    private val openSwap: ((wallet: WalletItem) -> Unit),
    private val navigateToFragment: ((url: String) -> Unit)
) : BottomSheetDialogFragment() {

    companion object {
        const val DETAIL_WALLET = "DetailWallet"
        fun newInstance(
            walletItem: WalletItem,
            openQRScanner: ((WalletItem) -> Unit),
            openAddCoin: (() -> Unit),
            openSendCoin: ((WalletItem) -> Unit),
            openReveice: (() -> Unit),
            openSwapCoin: ((wallet: WalletItem) -> Unit),
            navigateToFragment: ((url: String) -> Unit)

        ): DetailWalletBottomSheet {
            return DetailWalletBottomSheet(
                walletItem, openQRScanner, openAddCoin, openSendCoin, openReveice, openSwapCoin, navigateToFragment
            )
        }
    }

    private val detailWalletViewModel: DetailWalletViewModel by viewModel()
    private val binding: DialogDetailActivityBinding by viewBinding()

    private lateinit var activityAdapter: ActivityAdapter
    private var selectedTextView: AppCompatTextView? = null
    private val cal = Calendar.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_detail_activity, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        walletItem.run {
            detailWalletViewModel.getActivityList(
                this.depositAddress, icon, tokenName, this.tokenSymbol
            )
            detailWalletViewModel.getChartData(this.tokenSymbol)
            detailWalletViewModel.getPercentages(this)
        }
        initView()
        initViewModel()
        initObserves()
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {
        binding.run {
            currencyIcon.imageSource(walletItem.icon)
            vTitle.text = walletItem.tokenName
            vWalletAddress.text = walletItem.depositAddress
            vPrice.text = "$${walletItem.price.roundCurrencyValue()}"
            vTokenValue.text = "${walletItem.tokenSymbol} ${walletItem.amount}"
            vWalletAddress.setOnClickListener {
                context?.run { copyClipboard(vWalletAddress.text.toString()) }
            }
            getChartByOneHour.setOnClickListener {
                getChartByOneHour.changeTextColor(selectedTextView)
                val oneHour = getOneHour()
                walletItem.tokenSymbol.run {
                    detailWalletViewModel.getChartDataByDate(this, oneHour, cal.timeInMillis)
                }
                selectedTextView = getChartByOneHour
            }
            getChartByFourHour.setOnClickListener {
                getChartByFourHour.changeTextColor(selectedTextView)
                val fourHour = getFourHour()
                walletItem.tokenSymbol.run {
                    detailWalletViewModel.getChartDataByDate(this, fourHour, cal.timeInMillis)
                }
                selectedTextView = getChartByFourHour
            }
            getChartByDay.setOnClickListener {
                getChartByDay.changeTextColor(selectedTextView)
                val day = getYesterday()
                walletItem.tokenSymbol.run {
                    detailWalletViewModel.getChartDataByDate(this, day, cal.timeInMillis)
                }
                selectedTextView = getChartByDay
            }
            getChartByWeek.setOnClickListener {
                getChartByWeek.changeTextColor(selectedTextView)
                val week = getWeekly()
                walletItem.tokenSymbol.run {
                    detailWalletViewModel.getChartDataByDate(this, week, cal.timeInMillis)
                }
                selectedTextView = getChartByWeek
            }
            getChartByMonth.setOnClickListener {
                getChartByMonth.changeTextColor(selectedTextView)
                val month = getMonthly()
                walletItem.tokenSymbol.run {
                    detailWalletViewModel.getChartDataByDate(this, month, cal.timeInMillis)
                }
                selectedTextView = getChartByMonth
            }

            sendCoinAddress.setOnClickListener { walletItem.let { item -> openSendCoin.invoke(item) } }
            vQrScanner.setOnClickListener { walletItem.let { item -> openQRScanner.invoke(item) } }
            reciveWallet.setOnClickListener { openReceveCoin.invoke() }
            addCoin.setOnClickListener { openAddCoin.invoke() }
            swap.setOnClickListener { openSwap.invoke(walletItem) }
        }
        activityAdapter = ActivityAdapter(mutableListOf(), detailWalletViewModel)
        binding.vRvActivity.apply {
            adapter = activityAdapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    R.drawable.horizontal_divider_grey_blue_10
                )
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initViewModel() {
        detailWalletViewModel.getChartData.observe(
            viewLifecycleOwner,
            {
                binding.vChartData.initChart(it)
            }
        )
        detailWalletViewModel.getActivityData.observe(
            viewLifecycleOwner,
            {
                activityAdapter.updateList(it)
            }
        )
        detailWalletViewModel.getActivityDataError.observe(
            viewLifecycleOwner,
            {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        )
        detailWalletViewModel.getPercentages.observe(
            viewLifecycleOwner,
            { change24hPercentages ->
                if (change24hPercentages < 0) {
                    binding.vTokenPercent.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.red_400
                        )
                    )
                    binding.vTokenPercent.text = String.format(
                        getString(R.string.for_24h_negative_detail_wallet),
                        change24hPercentages.roundCurrencyValue()
                    )
                } else {
                    binding.vTokenPercent.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.limegreen
                        )
                    )
                    binding.vTokenPercent.text = String.format(
                        getString(R.string.for_24h_positive_detail_wallet),
                        change24hPercentages.roundCurrencyValue()
                    )
                }
            }
        )
    }

    private fun initObserves() {
        detailWalletViewModel.command.observe(viewLifecycleOwner) { viewCommand ->
            when (viewCommand) {
                is Command.OpenTransactionDialogViewCommand -> {
                    TransactionBottomSheet.newInstance(
                        viewCommand.itemActivity
                    ) {
                        navigateToFragment.invoke(it)
                    }.show(
                        childFragmentManager,
                        TransactionBottomSheet.TRANSACTION_DIALOG
                    )
                }
            }
        }
    }
}