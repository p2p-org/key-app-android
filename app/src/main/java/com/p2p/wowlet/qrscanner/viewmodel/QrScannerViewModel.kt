package com.p2p.wowlet.qrscanner.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.p2p.wowlet.deprecated.viewmodel.BaseViewModel
import com.p2p.wowlet.dashboard.interactor.DashboardInteractor
import com.p2p.wowlet.dashboard.interactor.QrScannerInteractor
import com.p2p.wowlet.common.network.Result
import com.p2p.wowlet.dashboard.model.local.QrWalletType
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

    fun getAccountInfo(publicKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val data = qrScannerInteractor.getAccountInfo(publicKey)) {
                is Result.Success -> withContext(Dispatchers.Main) {
                    data.data?.let { mintAddress ->
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