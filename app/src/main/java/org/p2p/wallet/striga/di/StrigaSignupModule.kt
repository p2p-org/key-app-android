package org.p2p.wallet.striga.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.new
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.create
import org.p2p.wallet.R
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.infrastructure.network.NetworkModule.getRetrofit
import org.p2p.wallet.infrastructure.network.interceptor.StrigaProxyApiInterceptor
import org.p2p.wallet.striga.StrigaUserIdProvider
import org.p2p.wallet.striga.signup.repository.StrigaSignupDataDatabaseRepository
import org.p2p.wallet.striga.signup.repository.StrigaSignupDataLocalRepository
import org.p2p.wallet.striga.signup.repository.mapper.StrigaSignupDataMapper
import org.p2p.wallet.striga.user.api.StrigaApi
import org.p2p.wallet.striga.user.repository.StrigaUserRemoteRepository
import org.p2p.wallet.striga.user.repository.StrigaUserRepository
import org.p2p.wallet.striga.user.repository.StrigaUserRepositoryMapper

object StrigaSignupModule : InjectionModule {
    override fun create() = module {
        factoryOf(::StrigaSignupDataDatabaseRepository) bind StrigaSignupDataLocalRepository::class
        factoryOf(::StrigaSignupDataMapper)

        factoryOf(::StrigaUserIdProvider)
        factoryOf(::StrigaUserRepositoryMapper)
        factoryOf(::StrigaUserRemoteRepository) bind StrigaUserRepository::class

        single<StrigaApi> {
            val url = androidContext().getString(R.string.strigaProxyServiceBaseUrl)
            getRetrofit(
                baseUrl = url,
                tag = "StrigaProxyApi",
                interceptor = new(::StrigaProxyApiInterceptor)
            ).create()
        }
    }
}
