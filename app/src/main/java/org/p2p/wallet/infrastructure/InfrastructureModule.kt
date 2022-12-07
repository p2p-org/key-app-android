package org.p2p.wallet.infrastructure

import androidx.room.Room
import android.content.Context
import android.content.SharedPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.solanaj.utils.crypto.Pbkdf2HashGenerator
import org.p2p.core.glide.GlideManager
import org.p2p.wallet.appsflyer.AppsFlyerService
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
import org.p2p.wallet.solana.SolanaNetworkObserver
import org.p2p.wallet.updates.SocketUpdatesManager
import org.p2p.wallet.updates.UpdateHandler
import org.p2p.wallet.updates.UpdatesManager
import timber.log.Timber
import java.security.KeyStore
import java.util.concurrent.Executors
private const val TAG = "InfrastructureModule"

object InfrastructureModule : InjectionModule {

    private const val ACCOUNT_PREFS = "ACCOUNT_PREFS"

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

        // TODO PWN-5418 - extract misc data to separate prefs
        single {
            val name = "${androidContext().packageName}.prefs"
            val prefs = androidContext().getSharedPreferences(name, Context.MODE_PRIVATE)
            Timber.tag(TAG).i("$name = $prefs")
            prefs
        }
        single(named(ACCOUNT_PREFS)) {
            val prefsName = "${androidContext().packageName}.account_prefs"
            val prefs = androidContext().getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            Timber.tag(TAG).i("$prefsName = $prefs")
            prefs
        }

        single { KeyStore.getInstance("AndroidKeyStore") }
        factoryOf(::EncoderDecoderMarshmallow) bind EncoderDecoder::class

        // TODO PWN-5418 - extract data to separate prefs from org.p2p.wallet.prefs
        factory {
            val sharedPreferences: SharedPreferences = get()

            SecureStorage(
                KeyStoreWrapper(
                    encoderDecoder = get(),
                    keyStore = get(),
                    sharedPreferences = sharedPreferences
                ),
                sharedPreferences = sharedPreferences,
                gson = get()
            )
        } bind SecureStorageContract::class
        factory {
            AccountStorage(
                keyStoreWrapper = KeyStoreWrapper(
                    encoderDecoder = get(),
                    keyStore = get(),
                    sharedPreferences = get(named(ACCOUNT_PREFS))
                ),
                sharedPreferences = get(named(ACCOUNT_PREFS)),
                gson = get()
            )
        } bind AccountStorageContract::class

        single { org.p2p.core.glide.GlideManager(get()) }

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

        factoryOf(::Pbkdf2HashGenerator)
        single { AppsFlyerService(androidContext()) }

        singleOf(::SolanaNetworkObserver)
    }
}
