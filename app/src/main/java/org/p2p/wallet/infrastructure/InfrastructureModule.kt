package org.p2p.wallet.infrastructure

import android.content.Context
import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.uikit.glide.GlideManager
import org.p2p.wallet.common.crypto.keystore.EncoderDecoder
import org.p2p.wallet.common.crypto.keystore.EncoderDecoderMarshmallow
import org.p2p.wallet.common.crypto.keystore.KeyStoreWrapper
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.deeplinks.AppDeeplinksManager
import org.p2p.wallet.history.repository.local.db.dao.CloseAccountTransactionsDao
import org.p2p.wallet.history.repository.local.db.dao.CreateAccountTransactionsDao
import org.p2p.wallet.history.repository.local.db.dao.RenBtcBurnOrMintTransactionsDao
import org.p2p.wallet.history.repository.local.db.dao.SwapTransactionsDao
import org.p2p.wallet.history.repository.local.db.dao.TransactionsDao
import org.p2p.wallet.history.repository.local.db.dao.TransactionsDaoDelegate
import org.p2p.wallet.history.repository.local.db.dao.TransferTransactionsDao
import org.p2p.wallet.history.repository.local.db.dao.UnknownTransactionsDao
import org.p2p.wallet.infrastructure.account.AccountStorage
import org.p2p.wallet.infrastructure.account.AccountStorageContract
import org.p2p.wallet.infrastructure.db.WalletDatabase
import org.p2p.wallet.infrastructure.db.WalletDatabase.Companion.DATABASE_NAME
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.infrastructure.dispatchers.DefaultDispatchers
import org.p2p.wallet.infrastructure.security.SecureStorage
import org.p2p.wallet.infrastructure.security.SecureStorageContract
import org.p2p.wallet.infrastructure.update.TransactionSignatureHandler
import org.p2p.wallet.notification.AppNotificationManager
import org.p2p.wallet.push_notifications.repository.PushTokenRepository
import org.p2p.wallet.updates.SocketUpdatesManager
import org.p2p.wallet.updates.UpdateHandler
import org.p2p.wallet.updates.UpdatesManager
import java.security.KeyStore
import java.util.concurrent.Executors

enum class SharedPreferencesName(val prefsName: String) {
    AccountStorage("AccountSharedPreferences"),
    KeyStorage("KeyStoreSharedPreferences")
}

object InfrastructureModule : InjectionModule {

    override fun create() = module {
        single {
            Room
                .databaseBuilder(androidContext(), WalletDatabase::class.java, DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .setQueryExecutor(Executors.newCachedThreadPool())
                .build()
        }

        single { get<WalletDatabase>().tokenDao() }
        single { get<WalletDatabase>().sessionDao() }
        single { get<WalletDatabase>().closeAccountTransactionsDao() }
        single { get<WalletDatabase>().createAccountTransactionsDao() }
        single { get<WalletDatabase>().swapTransactionsDao() }
        single { get<WalletDatabase>().transferTransactionsDao() }
        single { get<WalletDatabase>().renBtcBurnOrMintTransactionsDao() }
        single { get<WalletDatabase>().unknownTransactionsDao() }

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

        single {
            val name = "${androidContext().packageName}.prefs"
            androidContext().getSharedPreferences(name, Context.MODE_PRIVATE)
        }

        single(named(SharedPreferencesName.AccountStorage)) {
            val name = "${androidContext().packageName}.account_prefs"
            androidContext().getSharedPreferences(name, Context.MODE_PRIVATE)
        }
        single(named(SharedPreferencesName.KeyStorage)) {
            val name = "${androidContext().packageName}.keystore_prefs"
            androidContext().getSharedPreferences(name, Context.MODE_PRIVATE)
        }

        single<EncoderDecoder> { EncoderDecoderMarshmallow(get(named(SharedPreferencesName.KeyStorage))) }

        single { KeyStore.getInstance("AndroidKeyStore") }

        single {
            KeyStoreWrapper(encoderDecoder = get(), keyStore = get())
        }

        factoryOf(::SecureStorage) bind SecureStorageContract::class
        factory {
            AccountStorage(
                keyStoreWrapper = get(),
                sharedPreferences = get(named(SharedPreferencesName.AccountStorage)),
                gson = get()
            )
        } bind AccountStorageContract::class

        single { GlideManager(get()) }

        single {
            val updateHandlers = get<List<UpdateHandler>>(named<UpdateHandler>())
            SocketUpdatesManager(get(), get(), get(), updateHandlers)
        } bind UpdatesManager::class
        single(named<UpdateHandler>()) {
            listOf(
                TransactionSignatureHandler(get())
            )
        }

        single { AppDeeplinksManager(get()) }

        single { AppNotificationManager(get(), get()) }

        single { DefaultDispatchers() } bind CoroutineDispatchers::class

        single { PushTokenRepository() }
    }
}
