package org.p2p.wallet.history.interactor.mapper

import org.p2p.core.utils.orZero
import org.p2p.wallet.common.date.toZonedDateTime
import org.p2p.wallet.history.api.model.RpcHistoryResponse
import org.p2p.wallet.history.api.model.RpcHistoryStatusResponse
import org.p2p.wallet.history.api.model.RpcHistoryTypeResponse
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.model.TransferType
import org.p2p.wallet.transaction.model.TransactionStatus
import org.p2p.wallet.user.repository.UserLocalRepository
import java.math.BigDecimal

class NewHistoryTransactionMapper(
    private val userLocalRepository: UserLocalRepository
) {

    fun fromNetwork(item: RpcHistoryResponse): HistoryTransaction {
        return when (item.transactionType) {
            RpcHistoryTypeResponse.SEND,
            RpcHistoryTypeResponse.RECEIVE -> parseTransferTransaction(item)
            RpcHistoryTypeResponse.SWAP -> TODO()
            RpcHistoryTypeResponse.STAKE -> TODO()
            RpcHistoryTypeResponse.UNSTAKE -> TODO()
            RpcHistoryTypeResponse.CREATE_ACCOUNT -> TODO()
            RpcHistoryTypeResponse.CLOSE_ACCOUNT -> TODO()
            RpcHistoryTypeResponse.MINT -> TODO()
            RpcHistoryTypeResponse.BURN -> TODO()
            RpcHistoryTypeResponse.UNKNOWN -> TODO()
        }
    }

    private fun parseTransferTransaction(item: RpcHistoryResponse): HistoryTransaction.Transfer {
        val transactionInfo = item.transactionInfo
        val tokenMint = transactionInfo.tokensResponse.tokensInfoResponse.mint
        val tokenData = tokenMint?.let { userLocalRepository.findTokenData(it) } ?: error("Token data is null")
        val tokenPrice = transactionInfo.tokensResponse.tokensInfoResponse.tokenPrice

        val tokenBalances = transactionInfo.tokensResponse.tokensBalanceResponse
        val total = BigDecimal(tokenBalances.balanceBefore).minus(BigDecimal(tokenBalances.balanceAfter)).abs()
        return HistoryTransaction.Transfer(
            signature = item.txTransaction,
            date = item.date.toZonedDateTime(),
            blockNumber = null,
            status = item.stauts.fromNetwork(),
            type = item.getTransferType(),
            senderAddress = item.transactionInfo.counterPartyResponse.address.orEmpty(),
            tokenData = tokenData,
            totalInUsd = BigDecimal(tokenPrice),
            total = total,
            destination = transactionInfo.counterPartyResponse.address.orEmpty(),
            fee = transactionInfo.feeInfoResponse.feeAmount?.toBigInteger().orZero()
        )
    }
}

//    private fun parseSwapTransaction(item: RpcHistoryResponse): HistoryTransaction.Swap {
//        return HistoryTransaction.Swap(
//            signature = item.txTransaction,
//            date = item.date.toZonedDateTime(),
//            blockNumber = null,
//            status = item.stauts.fromNetwork(),
//        )
//    }
// }

private fun RpcHistoryStatusResponse.fromNetwork(): TransactionStatus {
    return when (this) {
        RpcHistoryStatusResponse.SUCCESS -> TransactionStatus.COMPLETED
        RpcHistoryStatusResponse.FAIL -> TransactionStatus.ERROR
    }
}

private fun RpcHistoryResponse.getTransferType(): TransferType {
    return when (this.transactionType) {
        RpcHistoryTypeResponse.RECEIVE -> TransferType.RECEIVE
        else -> TransferType.SEND
    }
}
