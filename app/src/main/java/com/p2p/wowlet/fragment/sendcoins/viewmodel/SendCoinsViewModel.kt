package com.p2p.wowlet.fragment.sendcoins.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command.*
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.wowlet.domain.interactors.SendCoinInteractor
import com.wowlet.entities.local.CoinItem
import com.wowlet.entities.local.SendTransactionModel
import com.wowlet.entities.local.UserWalletType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SendCoinsViewModel(val sendCoinInteractor: SendCoinInteractor) : BaseViewModel() {

    private val _pages: MutableLiveData<List<UserWalletType>> by lazy { MutableLiveData() }
    val pages: LiveData<List<UserWalletType>> get() = _pages

    private val _getAddCoinData by lazy { MutableLiveData<MutableList<CoinItem>>() }
    val getAddCoinData: LiveData<MutableList<CoinItem>> get() = _getAddCoinData

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
        _getAddCoinData.value = listAddCoinData
    }

    val list = mutableListOf(
        UserWalletType("Wallet address", "jd9(Hdh982y982y98308093", false, R.drawable.ic_qr_scaner),
        UserWalletType("Wallet user", "@username", true, R.drawable.ic_account)
    )

    fun initData() {
        _pages.value = list
    }

    fun openMyWalletsDialog() {
        _command.value = MyWalletDialogViewCommand()
    }

    fun openDoneDialog() {
        _command.value = SendCoinDoneViewCommand()
    }

    fun navigateUp() {
        _command.value =
            NavigateUpViewCommand(R.id.action_navigation_send_coin_to_navigation_dashboard)
    }

    fun goToQrScanner() {
        _command.value =
            NavigateScannerViewCommand(R.id.action_navigation_send_coin_to_navigation_scanner)
    }

    fun sendCoin() {
        viewModelScope.launch(Dispatchers.IO) {
            sendCoinInteractor.sendCoin(SendTransactionModel("", 1000))
        }
    }
}