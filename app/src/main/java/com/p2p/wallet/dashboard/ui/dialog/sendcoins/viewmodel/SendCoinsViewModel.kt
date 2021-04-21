package com.p2p.wallet.dashboard.ui.dialog.sendcoins.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.p2p.wallet.R
import com.p2p.wallet.deprecated.viewcommand.Command.NavigateBlockChainViewCommand
import com.p2p.wallet.deprecated.viewcommand.Command.OpenMyWalletDialogViewCommand
import com.p2p.wallet.deprecated.viewcommand.Command.SendCoinDoneViewCommand
import com.p2p.wallet.deprecated.viewcommand.Command.SendCoinViewCommand
import com.p2p.wallet.deprecated.viewmodel.BaseViewModel
import com.p2p.wallet.dashboard.interactor.DashboardInteractor
import com.p2p.wallet.dashboard.interactor.SendCoinInteractor
import com.p2p.wallet.common.network.Result
import com.p2p.wallet.dashboard.model.local.ActivityItem
import com.p2p.wallet.dashboard.model.local.QrWalletType
import com.p2p.wallet.dashboard.model.local.SendTransactionModel
import com.p2p.wallet.dashboard.model.local.UserWalletType
import com.p2p.wallet.dashboard.model.local.Token
import com.p2p.wallet.utils.roundToThousandsCurrencyValue
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

    private val _getWalletData by lazy { MutableLiveData<List<Token>>() }
    val getWalletData: LiveData<List<Token>> get() = _getWalletData

    private val _walletItemData by lazy { MutableLiveData<Token>(Token()) }
    val walletItemData: LiveData<Token> get() = _walletItemData

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

    private val _savedWalletItemData by lazy { MutableLiveData(Token()) }
    val savedWalletItemData: LiveData<Token> get() = _savedWalletItemData

    private val _selectedCurrency by lazy { MutableLiveData("--") }
    val selectedCurrency: LiveData<String> get() = _selectedCurrency

    private val _clearWalletAddress by lazy { MutableLiveData<Boolean>() }
    val clearWalletAddress: LiveData<Boolean> get() = _clearWalletAddress

    private val _walletIconVisibility by lazy { MutableLiveData<Boolean>(false) }
    val walletIconVisibility: LiveData<Boolean> get() = _walletIconVisibility

    private val _saveEnteredAmount by lazy { MutableLiveData<Boolean>() }
    val saveEnteredAmount: LiveData<Boolean> get() = _saveEnteredAmount

    private val _isInsertedMoreThanAvailable by lazy { MutableLiveData(false) }
    val isInsertedMoreThanAvailable: LiveData<Boolean> get() = _isInsertedMoreThanAvailable

    private var availableAmountInSelectedCurrency: Double = 0.0

    fun getWalletItems() = viewModelScope.launch(Dispatchers.IO) {
        val walletList = dashboardInteractor.getYourWallets()
        withContext(Dispatchers.Main) {
            _getWalletData.value = walletList.wallets
        }
    }

    fun initData(mutableListOf: MutableList<UserWalletType>) {
        _pages.value = mutableListOf
    }

    fun openMyWalletsDialog() {
        _command.value = OpenMyWalletDialogViewCommand()
    }

    fun selectWalletItem(item: Token) {
        _walletItemData.value = item
    }

    fun selectFromConstWalletItems(item: QrWalletType) {
        val walletItem = Token(
            tokenSymbol = item.walletItem.tokenSymbol,
            tokenName = item.walletItem.tokenName,
            mintAddress = item.walletItem.mint,
            iconUrl = item.walletItem.icon,
            decimals = 0,
            depositAddress = "",
            walletBinds = 0.0,
            price = BigDecimal.ZERO,
            total = BigDecimal.ZERO
        )
        _walletItemData.value = walletItem
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
                url
            )
    }

    fun sendCoin(toPublicKey: String, lamprots: Long, tokenSymbol: String) {
        viewModelScope.launch(Dispatchers.IO) {
            when (
                val data = sendCoinInteractor.sendCoin(
                    SendTransactionModel(
                        toPublicKey,
                        lamprots,
                        tokenSymbol = tokenSymbol
                    )
                )
            ) {
                is Result.Success -> withContext(Dispatchers.Main) {
                    _successTransaction.value = data.data
                }
                is Result.Error -> withContext(Dispatchers.Main) {
                    _errorTransaction.value = data.errors.errorMessage
                }
            }
        }
    }

    fun setWalletData(walletItem: Token?) {
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
            currency = _walletItemData.value?.tokenSymbol.toString()
            inputCountDouble.div(walletBinding)
        } else {
            currency = "USD"
            inputCountDouble.times(walletBinding)
        }
        val inputCoinInTokensString = context.getString(R.string.around_amount_sol, inputCoinInTokens, currency)
        _inputCountInTokens.value = inputCoinInTokensString
    }

    fun insertAllBalance() {
        val yourBalance: Double = walletItemData.value?.total?.toDouble() ?: 0.0
        val walletBinds: Double = walletItemData.value?.walletBinds ?: 0.0
        val balance = if (_selectedCurrency.value == "USD") {
            (yourBalance * walletBinds).roundToThousandsCurrencyValue()
        } else {
            yourBalance
        }
        inputCount.value = balance.toBigDecimal().toString()
    }

    fun toggleInsertAmountCurrency() {
        if (_selectedCurrency.value == "USD") {
            _selectedCurrency.value = walletItemData.value?.tokenSymbol
        } else {
            _selectedCurrency.value = "USD"
        }
    }

    fun setSelectedCurrency(currency: String) {
        if (_selectedCurrency.value == "USD") {
            _selectedCurrency.value = "USD"
        } else {
            _selectedCurrency.value = currency
        }
    }

    fun clearWalletAddress() {
        _clearWalletAddress.value = true
    }

    fun disableClearWalletAddress() {
        _clearWalletAddress.value = false
    }

    fun setWalletIconVisibility(isVisible: Boolean) {
        _walletIconVisibility.value = isVisible
    }

    fun setAvailableAmountInSelectedCurrency(amount: Double) {
        availableAmountInSelectedCurrency = amount
    }

    fun isAmountBiggerThanAvailable(_amount: String) {
        val amount: Double = if (_amount == "." || _amount == "" || _amount == "null") 0.0 else _amount.toDouble()
        _isInsertedMoreThanAvailable.value = amount > availableAmountInSelectedCurrency
    }
}