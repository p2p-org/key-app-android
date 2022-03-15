package org.p2p.wallet.infrastructure.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.p2p.wallet.history.db.dao.*
import org.p2p.wallet.history.db.entities.*
import org.p2p.wallet.home.db.TokenDao
import org.p2p.wallet.home.db.TokenEntity
import org.p2p.wallet.infrastructure.db.WalletDatabase.Companion.DATABASE_VERSION
import org.p2p.wallet.renbtc.db.SessionDao
import org.p2p.wallet.renbtc.db.SessionEntity

@Database(
    entities = [
        TokenEntity::class,
        SessionEntity::class,

        CreateAccountTransactionEntity::class,
        CloseAccountTransactionEntity::class,
        SwapTransactionEntity::class,
        TransferTransactionEntity::class,
        RenBtcBurnOrMintTransactionEntity::class,
        UnknownTransactionEntity::class
    ],
    version = DATABASE_VERSION,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class WalletDatabase : RoomDatabase() {

    companion object {
        const val DATABASE_VERSION = 4
        const val DATABASE_NAME = "p2p.wallet"
    }

    abstract fun tokenDao(): TokenDao
    abstract fun sessionDao(): SessionDao

    abstract val createAccountTransactionsDao: CreateAccountTransactionsDao
    abstract val closeAccountTransactionsDao: CloseAccountTransactionsDao
    abstract val swapTransactionsDao: SwapTransactionsDao
    abstract val transferTransactionsDao: TransferTransactionsDao
    abstract val renBtcBurnOrMintTransactionsDao: RenBtcBurnOrMintTransactionsDao
    abstract val unknownTransactionsDao: UnknownTransactionsDao
}