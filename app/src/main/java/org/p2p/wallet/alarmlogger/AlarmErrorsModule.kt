package org.p2p.wallet.alarmlogger

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import retrofit2.create
import org.p2p.core.common.di.InjectionModule
import org.p2p.core.network.NetworkCoreModule.getRetrofit
import org.p2p.wallet.R
import org.p2p.wallet.alarmlogger.api.AlarmErrorsServiceApi
import org.p2p.wallet.alarmlogger.logger.AlarmErrorsLogger
import org.p2p.wallet.alarmlogger.model.AlarmSendErrorConverter
import org.p2p.wallet.alarmlogger.model.AlarmStrigaErrorConverter
import org.p2p.wallet.alarmlogger.model.AlarmSwapErrorConverter

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
        factoryOf(::AlarmSwapErrorConverter)
        factoryOf(::AlarmSendErrorConverter)
        factoryOf(::AlarmStrigaErrorConverter)
    }
}
