package com.p2p.wowlet.fragment.dashboard.viewmodel

import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command.*
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.p2p.wowlet.fragment.detailwallet.view.DetailWalletFragment.Companion.WALLET_ITEM
import com.wowlet.domain.interactors.DashboardInteractor
import com.wowlet.entities.local.AddCoinItem
import com.wowlet.entities.local.WalletItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode

class DashboardViewModel(val dashboardInteractor: DashboardInteractor) : BaseViewModel() {
    private val _getWalletData by lazy { MutableLiveData<List<WalletItem>>() }
    val getWalletData: LiveData<List<WalletItem>> get() = _getWalletData

    private val _getAddCoinData by lazy { MutableLiveData<List<AddCoinItem>>() }
    val getAddCoinData: LiveData<List<AddCoinItem>> get() = _getAddCoinData
    private val _getMinimumBalanceData by lazy { MutableLiveData<Long>() }
    val getMinimumBalanceData: LiveData<Long> get() = _getMinimumBalanceData
    private val _yourBalance by lazy { MutableLiveData<Double>(0.0) }
    val yourBalance: LiveData<Double> get() = _yourBalance

    init {
        getWalletItems()
    }

    fun getAddCoinList() {
        viewModelScope.launch(Dispatchers.IO) {
            val data = dashboardInteractor.getAddCoinList()
            withContext(Dispatchers.Main) {
                _getAddCoinData.value = data.addCoinList
                _getMinimumBalanceData.value = data.minimumBalance
            }
        }

    }

    fun showMindAddress(addCoinItem: AddCoinItem) {
        _getAddCoinData.value = dashboardInteractor.showSelectedMintAddress(addCoinItem)
    }

    private fun getWalletItems() {
        viewModelScope.launch(Dispatchers.IO) {
            val walletList = dashboardInteractor.getWallets()
            withContext(Dispatchers.Main) {
                _getWalletData.value = walletList.wallets
                _yourBalance.value = walletList.balance
            }
        }
    }

    fun finishApp() {
        _command.value = FinishAppViewCommand()
    }

    fun goToScannerFragment() {
        _command.value =
            NavigateScannerViewCommand(R.id.action_navigation_dashboard_to_navigation_scanner)
    }

    fun goToProfileDetailDialog() {
        _command.value =
            OpenProfileDetailDialogViewCommand()
    }

    fun goToDetailWalletFragment(wallet: WalletItem) {
        val bundle = bundleOf(WALLET_ITEM to wallet)
        _command.value =
            NavigateWalletViewCommand(
                R.id.action_navigation_dashboard_to_navigation_detail_wallet,
                bundle
            )
    }

    fun goToSendCoinFragment() {
        _command.value =
            NavigateSendCoinViewCommand(
                R.id.action_navigation_dashboard_to_navigation_send_coin,
                null
            )
    }

    fun goToSwapFragment() {
        _command.value =
            NavigateSwapViewCommand(R.id.action_navigation_dashboard_to_navigation_swap)
    }


    fun openAddCoinDialog() {
        _command.value = OpenAddCoinDialogViewCommand()
    }

    fun openProfileDialog() {
        _command.value = OpenProfileDialogViewCommand()
    }

    fun enterWalletDialog() {

        _getWalletData.value?.let {
            if (it.isNotEmpty()) {
                val qrCode = dashboardInteractor.generateQRrCode(it)
                _command.value = EnterWalletDialogViewCommand(qrCode)
            }
        }
    }

    fun clearSecretKey() {
        viewModelScope.launch(Dispatchers.IO) {
            dashboardInteractor.clearSecretKey()
        }
    }

    fun roundCurrencyValue(value: Double): Double {
        return BigDecimal(value).setScale(2, RoundingMode.HALF_EVEN).toDouble()
    }

}