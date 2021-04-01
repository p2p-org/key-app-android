package com.p2p.wowlet.fragment.qrscanner.viewmodel

import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.p2p.wowlet.dialog.sendcoins.view.SendCoinsBottomSheet
import com.p2p.wowlet.fragment.qrscanner.view.QrScannerFragment
import com.wowlet.domain.interactors.DashboardInteractor
import com.wowlet.domain.interactors.QrScannerInteractor
import com.wowlet.entities.Result
import com.wowlet.entities.local.QrWalletType
import com.wowlet.entities.local.WalletItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QrScannerViewModel(
    val qrScannerInteractor: QrScannerInteractor,
    val dashboardInteractor: DashboardInteractor
) : BaseViewModel() {
    private val _isCurrentAccount by lazy { MutableLiveData<QrWalletType>() }
    val isCurrentAccount: LiveData<QrWalletType> get() = _isCurrentAccount
    private val _isCurrentAccountError by lazy { MutableLiveData<String>() }
    val isCurrentAccountError: LiveData<String> get() = _isCurrentAccountError

    fun navigateUp() {
        _command.value =
            Command.NavigateUpBackStackCommand()
    }

    fun navigateBack() {
        _command.value =
            Command.NavigateUpViewCommand(
                R.id.action_navigation_scanner_to_navigation_dashboard
            )
    }

    fun goToSendCoinFragment(walletKey: String) {
        _command.value =
            Command.OpenSendCoinDialogViewCommand(
                walletAddress = walletKey
            )
    }

    fun getAccountInfo(publicKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val data = qrScannerInteractor.getAccountInfo(publicKey)) {
                is Result.Success -> withContext(Dispatchers.Main) {
                    data.data?.let { mintAddress->
                        when (val result = dashboardInteractor.checkWalletFromList(mintAddress)) {
                            is Result.Success -> {
                                _isCurrentAccount.value =
                                    result.data?.let { constWalletItem -> QrWalletType(constWalletItem, publicKey) }
                            }
                            is Result.Error -> {
                                _isCurrentAccountError.value = result.errors.errorMessage
                            }
                        }
                    }
                }
                is Result.Error -> withContext(Dispatchers.Main) {
                    _isCurrentAccountError.value = data.errors.errorMessage
                }
            }
        }
    }
}