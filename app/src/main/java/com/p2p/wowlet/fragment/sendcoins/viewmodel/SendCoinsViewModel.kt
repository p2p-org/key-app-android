package com.p2p.wowlet.fragment.sendcoins.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command.*
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.wowlet.domain.interactors.DashboardInteractor
import com.wowlet.domain.interactors.SendCoinInteractor
import com.wowlet.entities.Result
import com.wowlet.entities.local.SendTransactionModel
import com.wowlet.entities.local.UserWalletType
import com.wowlet.entities.local.WalletItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SendCoinsViewModel(
    val sendCoinInteractor: SendCoinInteractor,
    val dashboardInteractor: DashboardInteractor
) : BaseViewModel() {

    private val _pages: MutableLiveData<List<UserWalletType>> by lazy { MutableLiveData() }
    val pages: LiveData<List<UserWalletType>> get() = _pages

    private val _getWalletData by lazy { MutableLiveData<List<WalletItem>>() }
    val getWalletData: LiveData<List<WalletItem>> get() = _getWalletData
    private val _walletItemData by lazy { MutableLiveData<WalletItem>(WalletItem()) }
    val walletItemData: LiveData<WalletItem> get() = _walletItemData
    private val _successTransaction by lazy { MutableLiveData<String>() }
    val successTransaction: LiveData<String> get() = _successTransaction
    private val _errorTransaction by lazy { MutableLiveData<String>() }
    val errorTransaction: LiveData<String> get() = _errorTransaction

    fun getWalletItems() {
        viewModelScope.launch(Dispatchers.IO) {
            val walletList = dashboardInteractor.getWallets()
            withContext(Dispatchers.Main) {
                _getWalletData.value = walletList.wallets
            }
        }
    }

    fun initData(mutableListOf: MutableList<UserWalletType>) {
        _pages.value = mutableListOf
    }

    fun openMyWalletsDialog() {
        _command.value = OpenMyWalletDialogViewCommand()
    }

    fun selectWalletItem(item: WalletItem) {
        _walletItemData.value = item
    }

    fun navigateUp() {
        _command.value =
            NavigateUpViewCommand(R.id.action_navigation_send_coin_to_navigation_dashboard)
    }

    fun goToQrScanner() {
        _command.value =
            NavigateScannerViewCommand(R.id.action_navigation_send_coin_to_navigation_scanner)
    }

    fun sendCoinCommand() {
        _command.value =
            SendCoinViewCommand()
    }

    fun openDoneDialog() {
        _command.value = SendCoinDoneViewCommand()
    }

    fun sendCoin(toPublicKey: String, lamprots: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val data = sendCoinInteractor.sendCoin(
                SendTransactionModel(
                    toPublicKey,
                    lamprots
                )
            )) {
                is Result.Success -> withContext(Dispatchers.Main) {
                    _successTransaction.value = data.data
                }
                is Result.Error -> withContext(Dispatchers.Main) {
                    _errorTransaction.value = data.errors.errorMessage
                }
            }
        }
    }
}