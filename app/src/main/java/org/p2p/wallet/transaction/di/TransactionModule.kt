package org.p2p.wallet.transaction.di

import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.infrastructure.transactionmanager.impl.TransactionManagerImpl
import org.p2p.wallet.transaction.interactor.TransactionBuilderInteractor
import org.p2p.wallet.transaction.interactor.TransactionStatusInteractor

object TransactionModule : InjectionModule {

    override fun create() = module {
        single { TransactionManagerImpl(get(), get()) }
        single { TransactionStatusInteractor(get()) }
        factory { TransactionBuilderInteractor() }
    }
}
