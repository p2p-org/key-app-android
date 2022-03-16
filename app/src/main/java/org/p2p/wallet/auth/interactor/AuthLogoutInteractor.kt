package org.p2p.wallet.auth.interactor

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.launch
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.history.interactor.TransactionsHistoryRepository
import org.p2p.wallet.home.repository.HomeLocalRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.infrastructure.security.SecureStorageContract
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.renbtc.RenTransactionManager
import org.p2p.wallet.renbtc.interactor.RenBtcInteractor
import org.p2p.wallet.renbtc.service.RenVMService
import org.p2p.wallet.updates.UpdatesManager

class AuthLogoutInteractor(
    private val context: Context,
    private val secureStorage: SecureStorageContract,
    private val renBtcInteractor: RenBtcInteractor,
    private val sharedPreferences: SharedPreferences,
    private val tokenKeyProvider: TokenKeyProvider,
    private val mainLocalRepository: HomeLocalRepository,
    private val updatesManager: UpdatesManager,
    private val transactionManager: RenTransactionManager,
    private val transactionsHistoryRepository: TransactionsHistoryRepository,
    private val appScope: AppScope,
) {
    suspend fun onUserLogout() {
        updatesManager.stop()
        sharedPreferences.edit { clear() }
        tokenKeyProvider.clear()
        secureStorage.clear()
        transactionManager.stop()
        mainLocalRepository.clear()
        renBtcInteractor.clearSession()
        transactionsHistoryRepository.deleteHistory()
        IntercomService.logout()
        RenVMService.stopService(context)
    }

    fun clearAppData() {
        appScope.launch {
            sharedPreferences.edit { clear() }
            secureStorage.clear()
            tokenKeyProvider.clear()
            mainLocalRepository.clear()
            updatesManager.stop()
            transactionManager.stop()
            renBtcInteractor.clearSession()
            transactionsHistoryRepository.deleteHistory()
            RenVMService.stopService(context)
        }
    }
}