package com.p2p.wowlet.dialog.sendcoins.viewmodel

import android.content.Context
import android.util.Log
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command.*
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.p2p.wowlet.fragment.blockchainexplorer.view.BlockChainExplorerFragment
import com.p2p.wowlet.utils.roundToThousandsCurrencyValue
import com.wowlet.domain.interactors.DashboardInteractor
import com.wowlet.domain.interactors.SendCoinInteractor
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

    private val _inputCountInTokens by lazy { MutableLiveData<String>() }
    val inputCountInTokens: LiveData<String> get() = _inputCountInTokens

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

    private val _selectedCurrency by lazy { MutableLiveData("USD") }
    val selectedCurrency: LiveData<String> get() = _selectedCurrency


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
            NavigateScannerFromSendCoinViewCommand()
    }

    fun sendCoinCommand() {
        _command.value =
            SendCoinViewCommand()
    }

    fun openDoneDialog(transactionInfo: ActivityItem) {
        _command.value = SendCoinDoneViewCommand(transactionInfo)
    }

    fun goToBlockChainExplorer(actionId: Int, url: String) {
        _command.value =
            NavigateBlockChainViewCommand(
                actionId,
                bundleOf(BlockChainExplorerFragment.BLOCK_CHAIN_URL to url)
            )
    }

    fun sendCoin(toPublicKey: String, lamprots: Long, tokenSymbol: String) {
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

    fun setInputCountInTokens(context: Context, inputCount: String) {
        if (inputCount.isEmpty()) {
            _inputCountInTokens.value = ""
            return
        }
        val inputCountDouble: Double = inputCount.toDouble()
        val walletBinding: Double? = _walletItemData.value?.walletBinds
        if (walletBinding == null || walletBinding == 0.0) {
            _inputCountInTokens.value = ""
            return
        }

        var currency = ""
        val inputCoinInTokens = if (_selectedCurrency.value == "USD") {
            currency = _walletItemData.value?.tokenName.toString()
            inputCountDouble.div(walletBinding)
        }else {
            currency = "USD"
            inputCountDouble.times(walletBinding)

        }
        val inputCoinInTokensString = context.getString(R.string.around_amount_sol, inputCoinInTokens, currency)
        _inputCountInTokens.value = inputCoinInTokensString

    }

    fun insertAllBalance() {
        val yourBalance: Double = walletItemData.value?.amount ?: 0.0
        val walletBinds: Double = walletItemData.value?.walletBinds ?: 0.0
        val balance =  if (_selectedCurrency.value == "USD") {
            (yourBalance*walletBinds).roundToThousandsCurrencyValue()
        }else {
            yourBalance
        }
        inputCount.value = balance.toBigDecimal().toString()
    }

    fun toggleInsertAmountCurrency() {
        if (_selectedCurrency.value == "USD") {
            _selectedCurrency.value = walletItemData.value?.tokenName
        }else {
            _selectedCurrency.value = "USD"
        }
    }
}