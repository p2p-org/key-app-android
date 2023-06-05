package org.p2p.wallet.auth.interactor

import androidx.core.content.edit
import androidx.work.WorkManager
import android.content.Context
import android.content.SharedPreferences
import timber.log.Timber
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.p2p.wallet.common.analytics.Analytics
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.history.repository.local.TransactionDetailsLocalRepository
import org.p2p.wallet.home.repository.HomeLocalRepository
import org.p2p.wallet.infrastructure.network.provider.SendModeProvider
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.infrastructure.security.SecureStorageContract
import org.p2p.wallet.infrastructure.swap.JupiterSwapStorageContract
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.send.repository.RecipientsLocalRepository
import org.p2p.wallet.push_notifications.interactor.PushNotificationsInteractor
import org.p2p.wallet.renbtc.RenTransactionManager
import org.p2p.wallet.renbtc.interactor.RenBtcInteractor
import org.p2p.wallet.renbtc.service.RenVMService
import org.p2p.wallet.updates.SubscriptionUpdatesManager

class AuthLogoutInteractor(
    private val context: Context,
    private val secureStorage: SecureStorageContract,
    private val renBtcInteractor: RenBtcInteractor,
    private val sharedPreferences: SharedPreferences,
    private val jupiterSwapStorage: JupiterSwapStorageContract,
    private val tokenKeyProvider: TokenKeyProvider,
    private val sendModeProvider: SendModeProvider,
    private val mainLocalRepository: HomeLocalRepository,
    private val recipientsLocalRepository: RecipientsLocalRepository,
    private val updatesManager: SubscriptionUpdatesManager,
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
            sendModeProvider.clear()
            secureStorage.clear()
            transactionManager.stop()
            mainLocalRepository.clear()
            recipientsLocalRepository.clear()
            renBtcInteractor.clearSession()
            transactionDetailsLocalRepository.deleteAll()
            jupiterSwapStorage.clear()
            IntercomService.logout()
            RenVMService.stopService(context)

            pushNotificationsInteractor.deleteDeviceToken(publicKey)
        }.invokeOnCompletion {
            appScope.cancel()
            WorkManager.getInstance(context).cancelAllWork()
        }
    }
}
