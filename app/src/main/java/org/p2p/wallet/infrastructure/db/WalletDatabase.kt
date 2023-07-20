package org.p2p.wallet.infrastructure.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.p2p.wallet.history.repository.local.db.dao.CloseAccountTransactionsDao
import org.p2p.wallet.history.repository.local.db.dao.CreateAccountTransactionsDao
import org.p2p.wallet.history.repository.local.db.dao.RenBtcBurnOrMintTransactionsDao
import org.p2p.wallet.history.repository.local.db.dao.SwapTransactionsDao
import org.p2p.wallet.history.repository.local.db.dao.TransferTransactionsDao
import org.p2p.wallet.history.repository.local.db.dao.UnknownTransactionsDao
import org.p2p.wallet.history.repository.local.db.entities.CloseAccountTransactionEntity
import org.p2p.wallet.history.repository.local.db.entities.CreateAccountTransactionEntity
import org.p2p.wallet.history.repository.local.db.entities.RenBtcBurnOrMintTransactionEntity
import org.p2p.wallet.history.repository.local.db.entities.SwapTransactionEntity
import org.p2p.wallet.history.repository.local.db.entities.TransferTransactionEntity
import org.p2p.wallet.history.repository.local.db.entities.UnknownTransactionEntity
import org.p2p.wallet.home.db.TokenDao
import org.p2p.wallet.home.db.TokenEntity
import org.p2p.wallet.infrastructure.db.WalletDatabase.Companion.DATABASE_VERSION
import org.p2p.wallet.infrastructure.sendvialink.db.UserSendLinkEntity
import org.p2p.wallet.infrastructure.sendvialink.db.UserSendLinksDao
import org.p2p.wallet.newsend.db.RecipientEntity
import org.p2p.wallet.newsend.db.RecipientsDao
import org.p2p.wallet.striga.signup.repository.dao.StrigaSignupDataDao
import org.p2p.wallet.striga.signup.repository.dao.StrigaSignupDataEntity

@Database(
    entities = [
        TokenEntity::class,

        CreateAccountTransactionEntity::class,
        CloseAccountTransactionEntity::class,
        SwapTransactionEntity::class,
        TransferTransactionEntity::class,
        RenBtcBurnOrMintTransactionEntity::class,
        UnknownTransactionEntity::class,

        RecipientEntity::class,

        UserSendLinkEntity::class,

        StrigaSignupDataEntity::class
    ],
    version = DATABASE_VERSION,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class WalletDatabase : RoomDatabase() {

    companion object {
        const val DATABASE_VERSION = 16
        const val DATABASE_NAME = "p2p.wallet"
    }

    abstract fun tokenDao(): TokenDao

    abstract fun createAccountTransactionsDao(): CreateAccountTransactionsDao
    abstract fun closeAccountTransactionsDao(): CloseAccountTransactionsDao
    abstract fun swapTransactionsDao(): SwapTransactionsDao
    abstract fun transferTransactionsDao(): TransferTransactionsDao
    abstract fun renBtcBurnOrMintTransactionsDao(): RenBtcBurnOrMintTransactionsDao
    abstract fun unknownTransactionsDao(): UnknownTransactionsDao

    abstract fun recipientsDao(): RecipientsDao
    abstract fun userSendLinksDao(): UserSendLinksDao
    abstract fun strigaSignupDao(): StrigaSignupDataDao
}
