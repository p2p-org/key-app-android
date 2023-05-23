package org.p2p.wallet.striga.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.striga.repository.StrigaKycDataInMemoryRepository
import org.p2p.wallet.striga.repository.StrigaKycDataLocalRepository
import org.p2p.wallet.striga.repository.StrigaSignupDataDatabaseRepository
import org.p2p.wallet.striga.repository.StrigaSignupDataLocalRepository
import org.p2p.wallet.striga.repository.mapper.StrigaSignupDataMapper

object StrigaSignupModule : InjectionModule {
    override fun create() = module {
        singleOf(::StrigaKycDataInMemoryRepository) bind StrigaKycDataLocalRepository::class
        factoryOf(::StrigaSignupDataDatabaseRepository) bind StrigaSignupDataLocalRepository::class
        factoryOf(::StrigaSignupDataMapper)
    }
}
