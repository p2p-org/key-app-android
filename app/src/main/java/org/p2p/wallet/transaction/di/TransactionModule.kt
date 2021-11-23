package org.p2p.wallet.transaction.di

import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.transaction.TransactionSendManager
import org.p2p.wallet.transaction.interactor.TransactionInteractor

object TransactionModule : InjectionModule {

    override fun create() = module {
        single { TransactionSendManager(get()) }
        single { TransactionInteractor(get()) }
    }
}