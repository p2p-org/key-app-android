package org.p2p.wallet.infrastructure.transactionmanager

import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.infrastructure.transactionmanager.impl.TransactionManagerImpl
import org.p2p.wallet.infrastructure.transactionmanager.impl.TransactionWorker
import org.p2p.wallet.infrastructure.transactionmanager.repository.TransactionQueueRepository
import org.p2p.wallet.infrastructure.transactionmanager.repository.TransactionQueueRepositoryImpl

object TransactionManagerModule : InjectionModule {

    override fun create(): Module = module {
        worker {
            TransactionWorker(
                context = androidContext(),
                workerParams = get(),
                rpcRepository = get(),
                appNotificationManager = get(),
                transactionQueueRepository = get()
            )
        }
        singleOf(::TransactionQueueRepositoryImpl) bind TransactionQueueRepository::class
        singleOf(::TransactionManagerImpl) bind TransactionManager::class
    }
}
