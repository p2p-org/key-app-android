package com.p2p.wowlet.fragment.dashboard.dialog.swap.viewmodel

import android.graphics.Color
import androidx.annotation.ColorRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.p2p.wowlet.R
import  com.p2p.wowlet.appbase.viewcommand.Command.*
import  com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.p2p.wowlet.utils.roundCurrencyValue
import com.p2p.wowlet.utils.roundToBilCurrencyValue
import com.p2p.wowlet.utils.roundToMilCurrencyValue
import com.wowlet.domain.interactors.SwapInteractor
import com.wowlet.entities.local.ActivityItem
import com.wowlet.entities.local.CoinItem
import com.wowlet.entities.local.WalletItem
import java.lang.NullPointerException
import java.math.BigDecimal

class SwapViewModel(
    private val swapInteractor: SwapInteractor
) : BaseViewModel() {


    private val _selectedWalletFrom by lazy { MutableLiveData<WalletItem>() }
    val selectedWalletFrom: LiveData<WalletItem> get() = _selectedWalletFrom

    private val _selectedWalletTo by lazy { MutableLiveData<WalletItem>() }
    val selectedWalletTo: LiveData<WalletItem> get() = _selectedWalletTo

    private val _isInCryptoCurrency by lazy { MutableLiveData<Boolean>(true) }
    val isInCryptoCurrency: LiveData<Boolean> get() = _isInCryptoCurrency

    private val _amountInConvertingToken by lazy { MutableLiveData<String>("0,0000") }
    val amountInConvertingToken: LiveData<String> get() = _amountInConvertingToken

    val amountBinding by lazy { MutableLiveData("") }
    val amount: LiveData<String> get() = amountBinding

    private val _aroundToCurrency by lazy { MutableLiveData<Double>(0.0) }
    val aroundToCurrency: LiveData<Double> get() = _aroundToCurrency

    private val _clearSearchBar by lazy { MutableLiveData(false) }
    val clearSearchBar: LiveData<Boolean> get() = _clearSearchBar

    private val _makeDialogFullScreen by lazy { MutableLiveData<Boolean>() }
    val makeDialogFullScreen: LiveData<Boolean> get() = _makeDialogFullScreen

    private val _selectedSlippage by lazy { MutableLiveData<Boolean>() }
    val selectedSlippage: LiveData<Boolean> get() = _selectedSlippage

    private val _isCustomSlippageEditorVisible by lazy { MutableLiveData<Boolean>(false) }
    val isCustomSlippageEditorVisible: LiveData<Boolean> get() = _isCustomSlippageEditorVisible

    private val _isFocusOnCustomSlippageEditor by lazy { MutableLiveData<Boolean>(false) }
    val isFocusOnCustomSlippageEditor: LiveData<Boolean> get() = _isFocusOnCustomSlippageEditor

    private val _isSlippageEditorEmpty by lazy { MutableLiveData<Boolean>(true) }
    val isSlippageEditorEmpty: LiveData<Boolean> get() = _isSlippageEditorEmpty

    private val _clearSlippageEditor by lazy { MutableLiveData<Boolean>() }
    val clearSlippageEditor: LiveData<Boolean> get() = _clearSlippageEditor

    private val _isFromPerTo by lazy { MutableLiveData<Boolean>(true) }
    val isFromPerTo: LiveData<Boolean> get() = _isFromPerTo

    private val _insufficientFoundsError by lazy { MutableLiveData<Boolean>() }
    val insufficientFoundsError: LiveData<Boolean> get() = _insufficientFoundsError

    private val _tintOnSearchBarFocusChange by lazy { MutableLiveData<Int>(R.color.gray_blue_400) }
    val tintOnSearchBarFocusChange: LiveData<Int> get() = _tintOnSearchBarFocusChange

    private val _isCloseIconVisible by lazy { MutableLiveData(false) }
    val isCloseIconVisible: LiveData<Boolean> get() = _isCloseIconVisible

    var tokenFromPerTokenTo: BigDecimal = 0.0.toBigDecimal()
    var tokenToPerTokenFrom: BigDecimal = 0.0.toBigDecimal()


    fun openMyWalletsDialog() {
        _command.value = OpenMyWalletDialogViewCommand()
    }

    private val listAddCoinData = mutableListOf(
        CoinItem(
            name = "Bitcoin",
            priceInUs = "12 000 US$",
            priceInBTC = "0,00212 BTC",
            type = "Wallet balance"
        ),
        CoinItem(
            name = "Tether",
            priceInUs = "12 000 US$",
            priceInBTC = "0,00212 BTC",
            type = "Wallet balance"
        ),
        CoinItem(
            name = "P2P wallet",
            priceInUs = "12 000 US$",
            priceInBTC = "0,00212 BTC",
            type = "Profile balance"
        ),
        CoinItem(name = "Savings"),
        CoinItem(
            name = "1UP",
            priceInUs = "12 000 US$",
            priceInBTC = "0,00212 BTC",
            type = "Investment"
        ),
        CoinItem(
            name = "0xBTC",
            priceInUs = "12 000 US$",
            priceInBTC = "0,00212 BTC",
            type = "Investment"
        )
    )

    fun getCoinList() {

    }

    fun openProcessingDialog() {
        val selectedWalletAmount: Double = if (_isInCryptoCurrency.value == true) {
            _selectedWalletFrom.value?.amount ?: 0.0
        } else {
            _selectedWalletFrom.value?.price ?: 0.0
        }
        val insertedAmount: Double = if (amount.value == "" || amount.value == ".") {
            0.0
        } else {
            amount.value?.toDouble()!!
        }
        if (selectedWalletAmount == 0.0 || insertedAmount == 0.0 || selectedWalletAmount < insertedAmount) {
            _insufficientFoundsError.value = true
            return
        }
        _command.value = SwapCoinProcessingViewCommand()
    }

    fun openDoneDialog(transactionInfo:ActivityItem) {
        _command.value = SendCoinDoneViewCommand(transactionInfo  )
    }

    fun openSelectTokenToSwapBottomSheet() {
        _command.value = OpenSelectTokenToSwapBottomSheet()
    }

    fun openSlippageSettingsBottomSheet() {
        _command.value = OpenSlippageSettingsBottomSheet()
    }

    fun navigateUp() {
        _command.value = NavigateUpBackStackCommand()
    }


    fun swapFromAmountCurrencyTypes() {
        val isCryptoCurrency = _isInCryptoCurrency.value ?: true
        _isInCryptoCurrency.value = !isCryptoCurrency
        amountBinding.value = amountBinding.value
    }

    fun setSelectedWalletFrom(walletItem: WalletItem) {
        _selectedWalletFrom.value = walletItem
        val to: Double = _selectedWalletTo.value?.walletBinds ?: return
        setTokenRatios(walletItem.walletBinds, to)
    }

    fun setSelectedWalletTo(walletItem: WalletItem) {
        _selectedWalletTo.value = walletItem
        val from = selectedWalletFrom.value?.walletBinds ?: return
        setTokenRatios(from, walletItem.walletBinds)
    }

    private fun setTokenRatios(from: Double, to: Double) {
        tokenFromPerTokenTo = swapInteractor.getTokenPerToken(from, to).roundToBilCurrencyValue()
        tokenToPerTokenFrom = swapInteractor.getTokenPerToken(to, from).roundToBilCurrencyValue()
    }


    fun clearSearchBar() {
        _clearSearchBar.value = true
    }

    fun makeDialogFullScreen() {
        _makeDialogFullScreen.value = true
    }

    fun setSelectedSlippage() {
        _selectedSlippage.value = true
    }

    fun makeCustomSlippageEditorVisible(isVisible: Boolean) {
        _isCustomSlippageEditorVisible.value = isVisible
    }

    fun setFocusOnSlippageEditor() {
        _isFocusOnCustomSlippageEditor.value = true
    }

    fun setIsSlippageEditorEmpty(isEmpty: Boolean) {
        _isSlippageEditorEmpty.value = isEmpty
    }

    fun clearSlippageEditor() {
        _clearSlippageEditor.value = true
    }

    fun switchTokenPrices() {
        _isFromPerTo.value = !_isFromPerTo.value!!
    }

    fun setTintOnSearchBarFocusChange(tint: Int) {
        _tintOnSearchBarFocusChange.value = tint
    }

    fun setCloseIconVisibility(isVisible: Boolean) {
        _isCloseIconVisible.value = isVisible
    }


    /**
     * @throws NullPointerException when WalletItem or _isInCryptoCurrency is null
     * Notice:Those cases expected to never happen
     */
    fun setAroundToCurrency(amount: String) {
        val walletBinds: Double = _selectedWalletFrom.value?.walletBinds ?: throw NullPointerException("WalletItem is null in Swap screen")
        val isInCryptoCurrency: Boolean = _isInCryptoCurrency.value ?: throw NullPointerException("_isInCryptoCurrency is null in Swap screen")
        _aroundToCurrency.value = swapInteractor.getAroundToCurrencyValue(amount, walletBinds, isInCryptoCurrency).roundCurrencyValue()
    }
    /**
     * @throws NullPointerException when _selectedWalletFrom is null
     * Notice:This case expected to never happen
     */
    fun setAmountOfConvertingToken(_amount: String) {
        var amount = _amount
        val to: Double = _selectedWalletTo.value?.walletBinds ?: return
        val from: Double = _selectedWalletFrom.value?.walletBinds ?: throw NullPointerException("WalletItem is null in Swap screen")
        if (_isInCryptoCurrency.value == false) {
            amount = _aroundToCurrency.value.toString()
        }
        val amountOfConvertingToken: BigDecimal = swapInteractor.getAmountInConvertingToken(amount, from, to).roundToMilCurrencyValue()
        val amountOfConvertingTokenStr: String = if (amountOfConvertingToken == 0.0.toBigDecimal()) "0.0000" else amountOfConvertingToken.toString()
        _amountInConvertingToken.value = amountOfConvertingTokenStr
    }




}