package org.p2p.wallet.transaction.di

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.named
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.core.common.di.InjectionModule
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager
import org.p2p.wallet.infrastructure.transactionmanager.impl.TransactionManagerImpl
import org.p2p.wallet.transaction.interactor.TransactionStatusInteractor
import org.p2p.wallet.transaction.progresshandler.BridgeSendProgressHandler
import org.p2p.wallet.transaction.progresshandler.ClaimProgressHandler
import org.p2p.wallet.transaction.progresshandler.SendSwapTransactionProgressHandler
import org.p2p.wallet.transaction.progresshandler.StrigaOffRampTransactionProgressHandler
import org.p2p.wallet.transaction.progresshandler.TransactionProgressHandler

object TransactionModule : InjectionModule {

    override fun create() = module {
        singleOf(::TransactionManagerImpl) bind TransactionManager::class
        factoryOf(::TransactionStatusInteractor)

        factoryOf(::SendSwapTransactionProgressHandler) {
            named(SendSwapTransactionProgressHandler.QUALIFIER)
            bind<TransactionProgressHandler>()
        }
        factoryOf(::ClaimProgressHandler) {
            named(ClaimProgressHandler.QUALIFIER)
            bind<TransactionProgressHandler>()
        }
        factoryOf(::BridgeSendProgressHandler) {
            named(BridgeSendProgressHandler.QUALIFIER)
            bind<TransactionProgressHandler>()
        }
        factoryOf(::StrigaOffRampTransactionProgressHandler) {
            named(StrigaOffRampTransactionProgressHandler.QUALIFIER)
            bind<TransactionProgressHandler>()
        }
    }
}
