package com.p2p.wowlet.fragment.sendcoins.viewmodel

import android.util.Log
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewcommand.Command.*
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.p2p.wowlet.fragment.blockchainexplorer.view.BlockChainExplorerFragment
import com.wowlet.domain.interactors.DashboardInteractor
import com.wowlet.domain.interactors.SendCoinInteractor
import com.wowlet.entities.Constants
import com.wowlet.entities.Result
import com.wowlet.entities.local.ActivityItem
import com.wowlet.entities.local.SendTransactionModel
import com.wowlet.entities.local.UserWalletType
import com.wowlet.entities.local.WalletItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode

class SendCoinsViewModel(
    val sendCoinInteractor: SendCoinInteractor,
    val dashboardInteractor: DashboardInteractor
) : BaseViewModel() {

    private val _pages: MutableLiveData<List<UserWalletType>> by lazy { MutableLiveData() }
    val pages: LiveData<List<UserWalletType>> get() = _pages

    private val _getWalletData by lazy { MutableLiveData<List<WalletItem>>() }
    val getWalletData: LiveData<List<WalletItem>> get() = _getWalletData
    private val _yourBalance by lazy { MutableLiveData(0.0) }
    val yourBalance: LiveData<Double> get() = _yourBalance
    private val _walletItemData by lazy { MutableLiveData<WalletItem>(WalletItem()) }
    val walletItemData: LiveData<WalletItem> get() = _walletItemData
    val inputCount: MutableLiveData<String> by lazy { MutableLiveData("") }
    private val _successTransaction by lazy { MutableLiveData<ActivityItem>() }
    val successTransaction: LiveData<ActivityItem> get() = _successTransaction

    private val _feeResponseLiveData by lazy { MutableLiveData<BigDecimal>() }
    val feeResponseLiveData: LiveData<BigDecimal> get() = _feeResponseLiveData

    private val _errorTransaction by lazy { MutableLiveData<String>() }
    val errorTransaction: LiveData<String> get() = _errorTransaction

    private val _feeErrorLiveData by lazy { MutableLiveData<String>() }
    val feeErrorLiveData: LiveData<String> get() = _feeErrorLiveData

    private val _savedWalletItemData by lazy { MutableLiveData(WalletItem()) }
    val savedWalletItemData: LiveData<WalletItem> get() = _savedWalletItemData

    fun getWalletItems() {
        viewModelScope.launch(Dispatchers.IO) {
            val walletList = dashboardInteractor.getYourWallets()
            withContext(Dispatchers.Main) {
                _getWalletData.value = walletList.wallets
                _yourBalance.value = walletList.balance
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
            NavigateUpBackStackCommand()
    }

    fun goToQrScanner() {
        _command.value =
            NavigateScannerViewCommand(R.id.action_navigation_send_coin_to_navigation_scanner)
    }

    fun sendCoinCommand() {
        _command.value =
            SendCoinViewCommand()
    }

    fun openDoneDialog(transactionInfo: ActivityItem) {
        _command.value = SendCoinDoneViewCommand(transactionInfo)
    }



    fun sendCoin(toPublicKey: String, lamprots: Long,tokenSymbol:String) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val data = sendCoinInteractor.sendCoin(
                SendTransactionModel(
                    toPublicKey,
                    lamprots,
                    tokenSymbol = tokenSymbol
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

    fun setWalletData(walletItem: WalletItem?) {
        sendCoinInteractor.saveWalletItem(walletItem)
    }

    fun getWalletData() {
        viewModelScope.launch(Dispatchers.IO) {
            when (val data = sendCoinInteractor.getWalletItem()) {
                is Result.Success -> withContext(Dispatchers.Main) {
                    _savedWalletItemData.value = data.data
                }
                is Result.Error -> withContext(Dispatchers.Main) {
                    Log.e("TAG", "getWalletData: ${data.errors.errorCode}")
                }
            }
        }
    }

    fun roundCurrencyValue(value: Double): Double {
        return BigDecimal(value).setScale(2, RoundingMode.HALF_EVEN).toDouble()
    }

    fun formatToken(value: Double, name: String): String {
        return (BigDecimal(value).stripTrailingZeros().toDouble().toString() + " " + name)
    }

    fun getFee() {
        viewModelScope.launch(Dispatchers.IO) {
            when (val data = sendCoinInteractor.getFee()) {
                is Result.Success -> withContext(Dispatchers.Main) {
                    _feeResponseLiveData.value = data.data
                }
                is Result.Error -> withContext((Dispatchers.Main)) {
                    _feeErrorLiveData.value = data.errors.errorMessage
                }
            }
        }
    }
    fun goToBlockChainExplorer(signature: String) {
        val blockChainExplorerUrl= Constants.EXPLORER_SOLANA +signature
        _command.value =
            NavigateBlockChainViewCommand(
                R.id.action_navigation_detail_wallet_to_navigation_block_chain_explorer,
                bundleOf(BlockChainExplorerFragment.BLOCK_CHAIN_URL to blockChainExplorerUrl)
            )
    }
}