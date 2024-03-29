package org.p2p.wallet.auth.username.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.create
import org.p2p.wallet.auth.username.api.RegisterUsernameServiceApi
import org.p2p.wallet.auth.username.repository.UsernameParser
import org.p2p.wallet.auth.username.repository.UsernameRemoteRepository
import org.p2p.wallet.auth.username.repository.UsernameRepository
import org.p2p.wallet.auth.username.repository.mapper.RegisterUsernameServiceApiMapper
import org.p2p.wallet.auth.username.repository.mapper.UsernameErrorMapper
import org.p2p.core.common.di.InjectionModule

object RegisterUsernameServiceModule : InjectionModule {
    const val REGISTER_USERNAME_SERVICE_RETROFIT_QUALIFIER = "REGISTER_USERNAME_SERVICE_RETROFIT_QUALIFIER"

    override fun create() = module {
        single<RegisterUsernameServiceApi> {
            get<Retrofit>(named(REGISTER_USERNAME_SERVICE_RETROFIT_QUALIFIER)).create()
        }
        factoryOf(::UsernameErrorMapper)
        factoryOf(::RegisterUsernameServiceApiMapper)
        factoryOf(::UsernameParser)
        singleOf(::UsernameRemoteRepository) bind UsernameRepository::class
    }
}
