package com.p2p.wowlet.fragment.swap.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.p2p.wowlet.R
import  com.p2p.wowlet.appbase.viewcommand.Command.*
import  com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.wowlet.entities.local.ActivityItem
import com.wowlet.entities.local.CoinItem
import com.wowlet.entities.local.WalletItem

class SwapViewModel : BaseViewModel() {

    private val _getAddCoinData by lazy { MutableLiveData<MutableList<WalletItem>>() }
    val getAddCoinData: LiveData<MutableList<WalletItem>> get() = _getAddCoinData

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
        _command.value = SwapCoinProcessingViewCommand()
    }

    fun openDoneDialog(transactionInfo:ActivityItem) {
        _command.value = SendCoinDoneViewCommand(transactionInfo  )
    }

    fun navigateUp() {
        _command.value = NavigateUpViewCommand(R.id.action_navigation_swap_to_navigation_dashboard)
    }

}