package org.p2p.wallet.history.model.rpc

import android.os.Parcelable
import org.threeten.bp.ZonedDateTime
import kotlinx.parcelize.IgnoredOnParcel
import org.p2p.wallet.transaction.model.TransactionStatus

sealed class HistoryTransaction(
    open val date: ZonedDateTime
) : Parcelable {

    abstract val signature: String
    abstract val blockNumber: Int?
    abstract val status: TransactionStatus

    protected fun getSymbol(isSend: Boolean): String = if (isSend) "-" else "+"

    fun getBlockNumber(): String? = blockNumber?.let { "#$it" }

    @IgnoredOnParcel
    val isFailed: Boolean
        get() = status == TransactionStatus.ERROR

    @IgnoredOnParcel
    val isPending: Boolean
        get() = status == TransactionStatus.PENDING






}
