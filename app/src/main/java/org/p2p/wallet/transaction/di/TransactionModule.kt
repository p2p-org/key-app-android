package org.p2p.wallet.transaction.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.core.common.di.InjectionModule
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager
import org.p2p.wallet.infrastructure.transactionmanager.impl.TransactionManagerImpl
import org.p2p.wallet.transaction.interactor.TransactionBuilderInteractor
import org.p2p.wallet.transaction.interactor.TransactionStatusInteractor

object TransactionModule : InjectionModule {

    override fun create() = module {
        singleOf(::TransactionManagerImpl) bind TransactionManager::class
        factoryOf(::TransactionStatusInteractor)
        factoryOf(::TransactionBuilderInteractor)
    }
}
