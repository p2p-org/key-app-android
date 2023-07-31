package org.p2p.wallet.alarmlogger

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.new
import org.koin.dsl.module
import retrofit2.create
import org.p2p.core.common.di.InjectionModule
import org.p2p.core.network.NetworkCoreModule.getRetrofit
import org.p2p.wallet.R
import org.p2p.wallet.alarmlogger.api.AlarmErrorsServiceApi
import org.p2p.wallet.alarmlogger.logger.AlarmErrorsLogger
import org.p2p.wallet.alarmlogger.model.AlarmBridgeErrorConverter
import org.p2p.wallet.alarmlogger.model.AlarmDeviceShareChangeErrorConverter
import org.p2p.wallet.alarmlogger.model.AlarmFeatureConverter
import org.p2p.wallet.alarmlogger.model.AlarmSendErrorConverter
import org.p2p.wallet.alarmlogger.model.AlarmStrigaErrorConverter
import org.p2p.wallet.alarmlogger.model.AlarmSwapErrorConverter
import org.p2p.wallet.alarmlogger.model.AlarmThrowableFormatter
import org.p2p.wallet.alarmlogger.model.AlarmWeb3ErrorConverter

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
        factory {
            // check the children size of  AlarmFeatureConverter to check DI is valid
            val converters: List<AlarmFeatureConverter> = listOf(
                new(::AlarmSwapErrorConverter),
                new(::AlarmSendErrorConverter),
                new(::AlarmDeviceShareChangeErrorConverter),
                new(::AlarmStrigaErrorConverter),
                new(::AlarmBridgeErrorConverter),
                new(::AlarmWeb3ErrorConverter),
            )
            AlarmErrorsLogger(
                api = get(),
                alarmConverters = converters,
                tokenKeyProvider = get(),
                ethInteractor = get(),
                appScope = get()
            )
        }

        factoryOf(::AlarmThrowableFormatter)
        factoryOf(::AlarmSwapErrorConverter)
        factoryOf(::AlarmSendErrorConverter)
        factoryOf(::AlarmDeviceShareChangeErrorConverter)
        factoryOf(::AlarmStrigaErrorConverter)
        factoryOf(::AlarmBridgeErrorConverter)
        factoryOf(::AlarmWeb3ErrorConverter)
    }
}
