package com.p2p.wowlet.fragment.dashboard.viewmodel

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.PieEntry
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewcommand.Command.*
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.p2p.wowlet.dialog.sendcoins.view.SendCoinsBottomSheet
import com.p2p.wowlet.fragment.blockchainexplorer.view.BlockChainExplorerFragment
import com.p2p.wowlet.fragment.dashboard.dialog.addcoin.AddCoinBottomSheet
import com.p2p.wowlet.fragment.pincode.view.PinCodeFragment.Companion.CREATE_NEW_PIN_CODE
import com.p2p.wowlet.fragment.pincode.view.PinCodeFragment.Companion.OPEN_FRAGMENT_BACKUP_DIALOG
import com.p2p.wowlet.fragment.qrscanner.view.QrScannerFragment
import com.p2p.wowlet.utils.roundCurrencyValue
import com.wowlet.domain.interactors.DashboardInteractor
import com.wowlet.domain.interactors.DetailWalletInteractor
import com.wowlet.domain.interactors.FingerPrintInteractor
import com.wowlet.entities.Result
import com.wowlet.entities.enums.PinCodeFragmentType
import com.wowlet.entities.local.AddCoinItem
import com.wowlet.entities.local.LocalWalletItem
import com.wowlet.entities.local.WalletItem
import com.wowlet.entities.local.YourWallets
import com.wowlet.entities.local.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.*
import kotlin.math.pow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectIndexed


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
    val nullWalletItem: WalletItem? = null

    fun getAddCoinList() {
        viewModelScope.launch(Dispatchers.IO) {
            val data = dashboardInteractor.getAddCoinList()
            withContext(Dispatchers.Main) {
                _getAddCoinData.value = data.addCoinList
            }
        }
    }

    fun showMindAddress(addCoinItem: AddCoinItem) {
        _getAddCoinData.value = dashboardInteractor.showSelectedMintAddress(addCoinItem)
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
            dashboardInteractor.saveEditedWallet(LocalWalletItem(walletId, changeName))
                .collect { value ->
                    withContext(Dispatchers.Main) {
                        _getWalletData.value = value
                    }
                }
        }
    }

    fun finishApp() {
        _command.value = FinishAppViewCommand()
    }

    fun goToScannerFragment() {
        if (getWalletData.value.isNullOrEmpty()) return
        _command.value =
            NavigateScannerViewCommand(
                R.id.action_navigation_dashboard_to_navigation_scanner
            )
    }

    fun goToProfileDetailDialog() {
        _command.value =
            OpenProfileDetailDialogViewCommand()
    }

    fun goToBackupDialog() {
        _command.value = OpenBackupDialogViewCommand()
    }

    fun goToCurrencyDialog(onCurrencySelected: () -> Unit) {
        _command.value = OpenCurrencyDialogViewCommand(onCurrencySelected)
    }

    fun goToSavedCardDialog() {
        _command.value = OpenSavedCardDialogViewCommand()
    }

    fun goToSecurityCardDialog(onFingerprintStateSelected: () -> Unit) {
        _command.value = OpenSecurityDialogViewCommand(onFingerprintStateSelected)
    }

    fun goToNetworkDialog(onNetworkSelected: () -> Unit) {
        _command.value = OpenNetworkDialogViewCommand(onNetworkSelected)
    }

    fun goToDetailWalletFragment(wallet: WalletItem) {
        _command.value = OpenWalletDetailDialogViewCommand(wallet)
    }


    fun goToSendCoinFragment() {
        _getAllWalletData.value?.let {
            if (it.isNotEmpty()) {
                _command.value =
                    OpenSendCoinDialogViewCommand(
                        walletItem = it[0]
                    )
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


    fun goToBlockChainExplorer(url: String) {
        _command.value =
            NavigateBlockChainViewCommand(
                R.id.action_navigation_dashboard_to_navigation_block_chain_explorer,
                bundleOf(BlockChainExplorerFragment.BLOCK_CHAIN_URL to url)
            )
    }

    fun goToPinCodeFragment() {
        _command.value =
            NavigatePinCodeViewCommand(
                R.id.action_navigation_dashboard_to_navigation_pin_code,
                bundleOf(
                    OPEN_FRAGMENT_BACKUP_DIALOG to true,
                    CREATE_NEW_PIN_CODE to PinCodeFragmentType.VERIFY
                )
            )
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

    fun openAddCoinDialog(updateAllMyTokens: () -> Unit = {}) {
        _command.value = OpenAddCoinDialogViewCommand(updateAllMyTokens)
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
