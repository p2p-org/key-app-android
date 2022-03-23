package org.p2p.wallet.infrastructure.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.p2p.wallet.history.db.dao.CloseAccountTransactionsDao
import org.p2p.wallet.history.db.dao.CreateAccountTransactionsDao
import org.p2p.wallet.history.db.dao.RenBtcBurnOrMintTransactionsDao
import org.p2p.wallet.history.db.dao.SwapTransactionsDao
import org.p2p.wallet.history.db.dao.TransferTransactionsDao
import org.p2p.wallet.history.db.dao.UnknownTransactionsDao
import org.p2p.wallet.history.db.entities.CloseAccountTransactionEntity
import org.p2p.wallet.history.db.entities.CreateAccountTransactionEntity
import org.p2p.wallet.history.db.entities.RenBtcBurnOrMintTransactionEntity
import org.p2p.wallet.history.db.entities.SwapTransactionEntity
import org.p2p.wallet.history.db.entities.TransferTransactionEntity
import org.p2p.wallet.history.db.entities.UnknownTransactionEntity
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
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WalletDatabase : RoomDatabase() {

    companion object {
        const val DATABASE_VERSION = 4
        const val DATABASE_NAME = "p2p.wallet"
    }

    abstract fun tokenDao(): TokenDao
    abstract fun sessionDao(): SessionDao

    abstract fun createAccountTransactionsDao(): CreateAccountTransactionsDao
    abstract fun closeAccountTransactionsDao(): CloseAccountTransactionsDao
    abstract fun swapTransactionsDao(): SwapTransactionsDao
    abstract fun transferTransactionsDao(): TransferTransactionsDao
    abstract fun renBtcBurnOrMintTransactionsDao(): RenBtcBurnOrMintTransactionsDao
    abstract fun unknownTransactionsDao(): UnknownTransactionsDao
}
