package com.p2p.wowlet.fragment.dashboard.viewmodel

import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command.*
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.p2p.wowlet.fragment.detailwallet.view.DetailWalletFragment.Companion.ICON
import com.p2p.wowlet.fragment.detailwallet.view.DetailWalletFragment.Companion.PUBLIC_KEY
import com.p2p.wowlet.fragment.detailwallet.view.DetailWalletFragment.Companion.TOKEN_NAME
import com.wowlet.domain.interactors.DashboardInteractor
import com.wowlet.entities.local.AddCoinItem
import com.wowlet.entities.local.WalletItem
import com.wowlet.entities.local.EnterWallet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardViewModel(val dashboardInteractor: DashboardInteractor) : BaseViewModel() {

    private val _pages: MutableLiveData<List<EnterWallet>> by lazy { MutableLiveData() }
    val pages: LiveData<List<EnterWallet>> get() = _pages

    private val _getWalletData by lazy { MutableLiveData<List<WalletItem>>() }
    val getWalletData: LiveData<List<WalletItem>> get() = _getWalletData

    private val _getAddCoinData by lazy { MutableLiveData<List<AddCoinItem>>() }
    val getAddCoinData: LiveData<List<AddCoinItem>> get() = _getAddCoinData
    private val _getMinimumBalanceData by lazy { MutableLiveData<Int>() }
    val getMinimumBalanceData: LiveData<Int> get() = _getMinimumBalanceData
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

    fun initReceiver() {
        val qrCode = dashboardInteractor.generateQRrCode()
        _pages.value = listOf(qrCode)
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

    fun goToDetailWalletFragment(depositAddress: String, icon: String, tokenName: String) {
        val bundle = bundleOf(PUBLIC_KEY to depositAddress, ICON to icon, TOKEN_NAME to tokenName)
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
        _command.value = EnterWalletDialogViewCommand()
    }

    fun clearSecretKey() {
        viewModelScope.launch(Dispatchers.IO) {
            dashboardInteractor.clearSecretKey()
        }
    }

}