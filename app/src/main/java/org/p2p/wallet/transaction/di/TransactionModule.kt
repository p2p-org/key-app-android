package org.p2p.wallet.transaction.di

import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.transaction.TransactionManager
import org.p2p.wallet.transaction.interactor.TransactionStatusInteractor

object TransactionModule : InjectionModule {

    override fun create() = module {
        single { TransactionManager(get()) }
        single { TransactionStatusInteractor(get()) }
    }
}
