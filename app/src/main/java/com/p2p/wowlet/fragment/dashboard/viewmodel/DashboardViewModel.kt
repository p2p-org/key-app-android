package com.p2p.wowlet.fragment.dashboard.viewmodel

import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.PieEntry
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command.*
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.p2p.wowlet.fragment.detailwallet.view.DetailWalletFragment.Companion.WALLET_ITEM
import com.p2p.wowlet.utils.roundCurrencyValue
import com.wowlet.domain.interactors.DashboardInteractor
import com.wowlet.entities.Result
import com.wowlet.entities.local.AddCoinItem
import com.wowlet.entities.local.WalletItem
import com.wowlet.entities.local.YourWallets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardViewModel(val dashboardInteractor: DashboardInteractor) : BaseViewModel() {
    private val _getWalletData by lazy { MutableLiveData<List<WalletItem>>() }
    val getWalletData: LiveData<List<WalletItem>> get() = _getWalletData
    private val _getAllWalletData by lazy { MutableLiveData<List<WalletItem>>() }
    val getAllWalletData: LiveData<List<WalletItem>> get() = _getAllWalletData


    private val _getWalletDataError by lazy { MutableLiveData<String>() }
    val getWalletDataError: LiveData<String> get() = _getWalletDataError

    private val _getWalletChart by lazy { MutableLiveData<List<PieEntry>>() }
    val getWalletChart: LiveData<List<PieEntry>> = _getWalletChart

    private val _getAddCoinData by lazy { MutableLiveData<List<AddCoinItem>>() }
    val getAddCoinData: LiveData<List<AddCoinItem>> get() = _getAddCoinData
    private val _getMinimumBalanceData by lazy { MutableLiveData<Long>() }
    val getMinimumBalanceData: LiveData<Long> get() = _getMinimumBalanceData
    private val _yourBalance by lazy { MutableLiveData<Double>(0.0) }
    val yourBalance: LiveData<Double> get() = _yourBalance

    private val _coinIsSuccessfullyAdded by lazy { MutableLiveData<AddCoinItem>() }
    val coinIsSuccessfullyAdded: LiveData<AddCoinItem> = _coinIsSuccessfullyAdded

    private val _coinNoAddedError by lazy { MutableLiveData<String>("") }
    val coinNoAddedError: LiveData<String> = _coinNoAddedError

    private var yourWallets: YourWallets? = null
    val progressData by lazy { MutableLiveData<Int>(0) }


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

    fun getWalletItems() {
        viewModelScope.launch(Dispatchers.IO) {
            when (val walletList = dashboardInteractor.getWallets()) {
                is Result.Success -> withContext(Dispatchers.Main) {
                    walletList.data?.let {
                        _getWalletData.value = it.mainWallets
                        _getAllWalletData.value = it.wallets
                        yourWallets = it
                        _yourBalance.value = it.balance
                        _getWalletChart.value = it.pieChartList
                    }
                }
                is Result.Error -> withContext(Dispatchers.Main) {
                    _getWalletDataError.value = walletList.errors.errorMessage
                }
            }
        }
    }


    fun getAllWallets() {
        viewModelScope.launch(Dispatchers.IO) {
            when (val walletList = dashboardInteractor.getAllWallets()) {
                is Result.Success -> withContext(Dispatchers.Main) {
                    walletList.data?.let {
                        _getWalletData.value = it
                    }
                }
                is Result.Error -> withContext(Dispatchers.Main) {
                    _getWalletDataError.value = walletList.errors.errorMessage
                }
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

    fun goToBackupDialog() {
        _command.value = OpenBackupDialogViewCommand()
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
        _getAllWalletData.value?.let {
            if (it.isNotEmpty()) {
                _command.value =
                    NavigateSendCoinViewCommand(
                        R.id.action_navigation_dashboard_to_navigation_send_coin,
                        null
                    )
            }
        }
    }

    fun goToSwapFragment() {
        _getAllWalletData.value?.let {
            if (it.isNotEmpty()) {
                _command.value =
                    NavigateSwapViewCommand(R.id.action_navigation_dashboard_to_navigation_swap)
            }
        }
    }

    fun addCoin(addCoinItem: AddCoinItem) {
        _coinNoAddedError.value = ""
        viewModelScope.launch(Dispatchers.IO) {
            progressStart()
            when (val data = dashboardInteractor.addCoin(addCoinItem)) {
                is Result.Success -> withContext(Dispatchers.Main) {
                    progressData.value = 100
                    _coinIsSuccessfullyAdded.value = data.data
                }
                is Result.Error -> withContext(Dispatchers.Main) {
                    _coinNoAddedError.value = data.errors.errorMessage
                }
            }
        }
    }

    private suspend fun progressStart() {
        withContext(Dispatchers.Main) {
            for (i in 1..90) {
                delay(500)
                progressData.value = i
            }
        }
    }

    fun openAddCoinDialog() {
        _command.value = OpenAddCoinDialogViewCommand()
    }

    fun openAllMyTokensDialog() {
        _command.value = yourWallets?.let { OpenAllMyTokensDialogViewCommand(it) }
    }

    fun openProfileDialog() {
        _command.value = OpenProfileDialogViewCommand()
    }

    fun enterWalletDialog() {

        _getAllWalletData.value?.let {
            if (it.isNotEmpty()) {
                val qrCodesList = dashboardInteractor.generateQRrCode(it)
                _command.value = EnterWalletDialogViewCommand(qrCodesList)
            }
        }
    }

    fun clearSecretKey() {
        viewModelScope.launch(Dispatchers.IO) {
            dashboardInteractor.clearSecretKey()
        }
    }

    fun roundCurrencyValue(value: Double): Double {
        return value.roundCurrencyValue()
    }

}