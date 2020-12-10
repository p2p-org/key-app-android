package com.p2p.wowlet.fragment.qrscanner.viewmodel

import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.p2p.wowlet.fragment.sendcoins.view.SendCoinsFragment.Companion.WALLET_ADDRESS
import com.wowlet.domain.interactors.QrScannerInteractor
import com.wowlet.entities.Result
import com.wowlet.entities.local.QrWalletType
import com.wowlet.entities.local.WalletItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QrScannerViewModel(val qrScannerInteractor: QrScannerInteractor) : BaseViewModel() {
    private val _isCurrentAccount by lazy { MutableLiveData<QrWalletType>() }
    val isCurrentAccount: LiveData<QrWalletType> get() = _isCurrentAccount
    private val _isCurrentAccountError by lazy { MutableLiveData<String>() }
    val isCurrentAccountError: LiveData<String> get() = _isCurrentAccountError

    fun navigateUp() {
        _command.value =
            Command.NavigateUpViewCommand(R.id.action_navigation_scanner_to_navigation_dashboard)
    }

    fun goToSendCoinFragment(walletAddress: String) {
        _command.value =
            Command.NavigateSendCoinViewCommand(
                R.id.action_navigation_scanner_to_navigation_send_coin,
                bundleOf(WALLET_ADDRESS to walletAddress)
            )
    }

    fun getAccountInfo(publicKey:String) {
        viewModelScope.launch(Dispatchers.IO){
            when(val data=qrScannerInteractor.getAccountInfo(publicKey)){
                is Result.Success-> withContext(Dispatchers.Main){
                    data.data?.let {
                        _isCurrentAccount.value=QrWalletType(it,publicKey)
                    }

                }
                is Result.Error -> withContext(Dispatchers.Main) {
                    _isCurrentAccountError.value = data.errors.errorMessage
                }
            }
        }
    }
}