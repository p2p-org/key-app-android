package org.p2p.wallet.infrastructure.network.alarmlogger

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import retrofit2.create
import org.p2p.wallet.R
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.infrastructure.network.NetworkModule.getRetrofit
import org.p2p.wallet.infrastructure.network.alarmlogger.api.AlarmErrorsServiceApi

object AlarmErrorsModule : InjectionModule {
    override fun create() = module {
        single<AlarmErrorsServiceApi> {
            val retrofit = getRetrofit(
                baseUrl = androidContext().getString(R.string.alarmErrorsServiceBaseUrl),
                tag = "AlarmErrors",
                interceptor = null
            )
            retrofit.create()
        }
        factoryOf(::AlarmErrorsLogger)
    }
}
