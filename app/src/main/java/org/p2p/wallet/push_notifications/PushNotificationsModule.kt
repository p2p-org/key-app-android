package org.p2p.wallet.push_notifications

import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.push_notifications.api.NotificationServiceApi
import org.p2p.wallet.push_notifications.interactor.PushNotificationsInteractor
import org.p2p.wallet.push_notifications.repository.DeviceTokenRemoteRepository
import org.p2p.wallet.push_notifications.repository.DeviceTokenRepository
import retrofit2.Retrofit
import retrofit2.create

object PushNotificationsModule : InjectionModule {

    const val NOTIFICATION_SERVICE_RETROFIT_QUALIFIER = "NOTIFICATION_SERVICE_RETROFIT_QUALIFIER"

    override fun create() = module {

        single<DeviceTokenRepository> {
            val api = get<Retrofit>(named(NOTIFICATION_SERVICE_RETROFIT_QUALIFIER)).create<NotificationServiceApi>()
            DeviceTokenRemoteRepository(api)
        }

        factory {
            PushNotificationsInteractor(
                deviceTokenRepository = get(),
                pushTokenRepository = get(),
                tokenKeyProvider = get(),
                sharedPreferences = get()
            )
        }
    }
}
