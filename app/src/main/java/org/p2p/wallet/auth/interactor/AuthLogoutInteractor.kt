package org.p2p.wallet.auth.interactor

import androidx.core.content.edit
import android.content.Context
import android.content.SharedPreferences
import org.p2p.wallet.common.analytics.Analytics
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.history.repository.local.TransactionDetailsLocalRepository
import org.p2p.wallet.home.repository.HomeLocalRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.infrastructure.security.SecureStorageContract
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.push_notifications.ineractor.PushNotificationsInteractor
import org.p2p.wallet.renbtc.RenTransactionManager
import org.p2p.wallet.renbtc.interactor.RenBtcInteractor
import org.p2p.wallet.renbtc.service.RenVMService
import org.p2p.wallet.updates.UpdatesManager
import timber.log.Timber
import kotlinx.coroutines.launch

class AuthLogoutInteractor(
    private val context: Context,
    private val secureStorage: SecureStorageContract,
    private val renBtcInteractor: RenBtcInteractor,
    private val sharedPreferences: SharedPreferences,
    private val tokenKeyProvider: TokenKeyProvider,
    private val mainLocalRepository: HomeLocalRepository,
    private val updatesManager: UpdatesManager,
    private val transactionManager: RenTransactionManager,
    private val transactionDetailsLocalRepository: TransactionDetailsLocalRepository,
    private val pushNotificationsInteractor: PushNotificationsInteractor,
    private val appScope: AppScope,
    private val analytics: Analytics
) {
    fun onUserLogout() {
        appScope.launch {
            val publicKey = tokenKeyProvider.publicKey
            Timber.i("Cleaning storages and stopping all services for $publicKey")

            analytics.clearUserProperties()
            analytics.setUserId(null)

            updatesManager.stop()
            sharedPreferences.edit { clear() }
            tokenKeyProvider.clear()
            secureStorage.clear()
            transactionManager.stop()
            mainLocalRepository.clear()
            renBtcInteractor.clearSession()
            transactionDetailsLocalRepository.deleteAll()
            IntercomService.logout()
            RenVMService.stopService(context)

            pushNotificationsInteractor.deleteDeviceToken(publicKey)
        }
    }
}
