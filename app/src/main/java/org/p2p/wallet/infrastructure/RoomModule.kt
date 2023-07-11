package org.p2p.wallet.infrastructure

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import java.util.concurrent.Executors
import org.p2p.wallet.history.repository.local.db.dao.CloseAccountTransactionsDao
import org.p2p.wallet.history.repository.local.db.dao.CreateAccountTransactionsDao
import org.p2p.wallet.history.repository.local.db.dao.RenBtcBurnOrMintTransactionsDao
import org.p2p.wallet.history.repository.local.db.dao.SwapTransactionsDao
import org.p2p.wallet.history.repository.local.db.dao.TransactionsDao
import org.p2p.wallet.history.repository.local.db.dao.TransactionsDaoDelegate
import org.p2p.wallet.history.repository.local.db.dao.TransferTransactionsDao
import org.p2p.wallet.history.repository.local.db.dao.UnknownTransactionsDao
import org.p2p.wallet.infrastructure.db.WalletDatabase

object RoomModule {
    fun create() = module {
        single {
            Room.databaseBuilder(androidContext(), WalletDatabase::class.java, WalletDatabase.DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .setQueryExecutor(Executors.newCachedThreadPool())
                .build()
        }

        single { get<WalletDatabase>().tokenDao() }
        single { get<WalletDatabase>().closeAccountTransactionsDao() }
        single { get<WalletDatabase>().createAccountTransactionsDao() }
        single { get<WalletDatabase>().swapTransactionsDao() }
        single { get<WalletDatabase>().transferTransactionsDao() }
        single { get<WalletDatabase>().renBtcBurnOrMintTransactionsDao() }
        single { get<WalletDatabase>().unknownTransactionsDao() }
        single { get<WalletDatabase>().recipientsDao() }

        single { get<WalletDatabase>().userSendLinksDao() }

        single { get<WalletDatabase>().strigaSignupDao() }

        single {
            val allTransactionDaos: List<TransactionsDao<*>> = listOf(
                get<CloseAccountTransactionsDao>(),
                get<CreateAccountTransactionsDao>(),
                get<SwapTransactionsDao>(),
                get<TransferTransactionsDao>(),
                get<RenBtcBurnOrMintTransactionsDao>(),
                get<UnknownTransactionsDao>(),
            )
            TransactionsDaoDelegate(allTransactionDaos)
        }
    }
}
