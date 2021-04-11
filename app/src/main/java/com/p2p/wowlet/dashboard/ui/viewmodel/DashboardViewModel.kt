package com.p2p.wowlet.dashboard.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.PieEntry
import com.p2p.wowlet.auth.interactor.FingerPrintInteractor
import com.p2p.wowlet.dashboard.ui.dialog.addcoin.AddCoinBottomSheet
import com.p2p.wowlet.deprecated.viewcommand.Command
import com.p2p.wowlet.deprecated.viewcommand.Command.EnterWalletDialogViewCommand
import com.p2p.wowlet.deprecated.viewcommand.Command.OpenAllMyTokensDialogViewCommand
import com.p2p.wowlet.deprecated.viewcommand.Command.OpenBackupFailedDialogViewCommand
import com.p2p.wowlet.deprecated.viewcommand.Command.OpenEditWalletDialogViewCommand
import com.p2p.wowlet.deprecated.viewcommand.Command.OpenProfileDialogViewCommand
import com.p2p.wowlet.deprecated.viewcommand.Command.OpenRecoveryPhraseDialogViewCommand
import com.p2p.wowlet.deprecated.viewcommand.Command.OpenSwapBottomSheetViewCommand
import com.p2p.wowlet.deprecated.viewcommand.Command.YourWalletDialogViewCommand
import com.p2p.wowlet.deprecated.viewmodel.BaseViewModel
import com.p2p.wowlet.dashboard.interactor.DashboardInteractor
import com.p2p.wowlet.dashboard.interactor.DetailWalletInteractor
import com.p2p.wowlet.common.network.Result
import com.p2p.wowlet.dashboard.model.local.AddCoinItem
import com.p2p.wowlet.dashboard.model.local.EnableFingerPrintModel
import com.p2p.wowlet.dashboard.model.local.LocalWalletItem
import com.p2p.wowlet.dashboard.model.local.WalletItem
import com.p2p.wowlet.dashboard.model.local.YourWallets
import com.p2p.wowlet.utils.roundCurrencyValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardViewModel(
    val dashboardInteractor: DashboardInteractor,
    val detailWalletInteractor: DetailWalletInteractor,
    val fingerPrintInteractor: FingerPrintInteractor
) : BaseViewModel() {
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

    private val _yourBalance by lazy { MutableLiveData<Double>(0.0) }
    val yourBalance: LiveData<Double> get() = _yourBalance

    private val _coinIsSuccessfullyAdded by lazy { MutableLiveData<AddCoinItem>() }
    val coinIsSuccessfullyAdded: LiveData<AddCoinItem> = _coinIsSuccessfullyAdded

    private val _coinNoAddedError by lazy { MutableLiveData<String>() }
    val coinNoAddedError: LiveData<String> = _coinNoAddedError

    private val _onCoinAdd by lazy { MutableLiveData<AddCoinItem>() }
    val onCoinAdd: LiveData<AddCoinItem> get() = _onCoinAdd

    private val _progressData by lazy { MutableLiveData<Int>() }
    val progressData: LiveData<Int> get() = _progressData

    val emptyLambda: () -> Unit = {}

    private var yourWallets: YourWallets? = null
    private var getWalletItemsJob: Job? = null

    fun getAddCoinList() {
        viewModelScope.launch(Dispatchers.IO) {
            val data = dashboardInteractor.getAddCoinList()
            withContext(Dispatchers.Main) {
                _getAddCoinData.value = data.addCoinList
            }
        }
    }

    fun getWalletItems() {
        clearWalletItems()
        getWalletItemsJob = viewModelScope.launch(Dispatchers.IO) {
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

    private fun clearWalletItems() {
        getWalletItemsJob?.cancel()
        _getWalletData.value = listOf()
        _getAllWalletData.value = listOf()
        yourWallets = YourWallets(listOf(), 0.0, listOf(), listOf())
        _yourBalance.value = 0.0
        _getWalletChart.value = listOf()
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

    fun clearGetWalletData() {
        _getWalletData.value = listOf()
    }

    fun setChangeWallet(walletId: String, changeName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dashboardInteractor.saveEditedWallet(LocalWalletItem(walletId, changeName)).collect { value ->
                withContext(Dispatchers.Main) {
                    _getWalletData.value = value
                }
            }
        }
    }

    fun goToQrScanner(walletItem: WalletItem) {
        val enterWallet = detailWalletInteractor.generateQRrCode(walletItem)
        _command.value = YourWalletDialogViewCommand(enterWallet)
    }

    fun openSwapBottomSheet(walletItem: WalletItem? = null) {
        _getAllWalletData.value?.let {
            if (it.isNotEmpty()) {
                val item: WalletItem = walletItem ?: it[0]
                _command.value =
                    OpenSwapBottomSheetViewCommand(item, it)
            }
        }
    }

    fun addCoin(addCoinItem: AddCoinItem) {
        _progressData.value = 0
        _onCoinAdd.value = addCoinItem
        viewModelScope.launch(Dispatchers.IO) {
            progressStart()
            when (val data = dashboardInteractor.addCoin(addCoinItem)) {
                is Result.Success -> withContext(Dispatchers.Main) {
                    _progressData.value = 100
                    _coinIsSuccessfullyAdded.value = addCoinItem
                }
                is Result.Error -> withContext(Dispatchers.Main) {
                    _progressData.value = 100
                    _coinNoAddedError.value = data.errors.errorMessage
                }
            }
        }
    }

    private suspend fun progressStart() {
        withContext(Dispatchers.Main) {
            for (i in 1..90) {
                delay(1000)
                _progressData.value = i
            }
        }
    }

    fun goToSendCoin(walletItem: WalletItem) {
        _command.value = Command.OpenSendCoinDialogViewCommand(
            walletItem = walletItem
        )
    }

    fun openAllMyTokensDialog() {
        _command.value = yourWallets?.let { OpenAllMyTokensDialogViewCommand(it) }
    }

    fun openProfileDialog() {
        _command.value = OpenProfileDialogViewCommand()
    }

    fun openRecoveryPhraseDialog() {
        _command.value = OpenRecoveryPhraseDialogViewCommand()
    }

    fun openBackupFailedDialog() {
        _command.value = OpenBackupFailedDialogViewCommand()
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

    fun updateNavValue(addCoinItem: AddCoinItem) {
        addCoinItem.navigatingBack = true
        _coinIsSuccessfullyAdded.value = addCoinItem
        _coinNoAddedError.value = AddCoinBottomSheet.NAV_TAG_COIN_NO_ADDED_ERROR
    }

    fun openEditWalletDialog(walletItem: WalletItem) {
        _command.value = OpenEditWalletDialogViewCommand(walletItem)
    }

    fun clearFingerprint() {
        viewModelScope.launch(Dispatchers.IO) {
            fingerPrintInteractor.setFingerPrint(
                EnableFingerPrintModel(isEnable = false, isNotWantEnable = false)
            )
        }
    }
}