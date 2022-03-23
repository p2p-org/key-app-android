package org.p2p.wallet.renbtc.service

import kotlinx.coroutines.delay
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.kits.renBridge.LockAndMint
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseQueryTxMint
import org.p2p.wallet.renbtc.model.MintStatus
import org.p2p.wallet.renbtc.model.RenTransaction
import org.p2p.wallet.renbtc.model.RenTransactionStatus
import org.p2p.wallet.renbtc.model.RenTransactionStatus.WaitingDepositConfirm
import org.p2p.wallet.receive.renbtc.BTC_DECIMALS
import org.p2p.wallet.utils.fromLamports
import timber.log.Timber

private const val ONE_MINUTE_DELAY = 1000L * 60L

class RenStatusExecutor(
    private val lockAndMint: LockAndMint,
    private val transaction: RenTransaction,
    private val secretKey: ByteArray
) : RenTransactionExecutor {

    companion object {
        private const val REN_TAG = "renBTCexecutor"
    }

    init {
        setStatus(WaitingDepositConfirm(transaction.payment.transactionHash))
    }

    override fun getTransactionHash(): String = transaction.payment.transactionHash

    override suspend fun execute() {
        if (transaction.isActive()) return

        lockAndMint.getDepositState(
            transaction.payment.transactionHash,
            transaction.payment.txIndex.toString(),
            transaction.payment.amount.toString()
        )
        val txHash = lockAndMint.submitMintTransaction()
        startQueryMintPolling(txHash, secretKey)
    }

    override fun isFinished(): Boolean = transaction.isFinished()

    private suspend fun startQueryMintPolling(txHash: String, secretKey: ByteArray) {
        while (true) {
            val response = lockAndMint.lockAndMint(txHash)
            if (transaction.isFinished()) {
                break
            }

            handleStatus(response, secretKey)
            delay(ONE_MINUTE_DELAY)
        }
    }

    private fun handleStatus(response: ResponseQueryTxMint, secretKey: ByteArray) {
        val status = response.txStatus
        val transactionId = "TransactionId: ${transaction.statuses.lastOrNull()?.transactionId}"
        Timber
            .tag(REN_TAG)
            .d("$transactionId: Current mint status: $status, will check again in one minute")

        val transactionHash = transaction.payment.transactionHash

        when (MintStatus.parse(status)) {
            MintStatus.CONFIRMED -> setStatus(RenTransactionStatus.AwaitingForSignature(transactionHash))
            MintStatus.EXECUTING -> setStatus(RenTransactionStatus.SubmittingToRenVM(transactionHash))
            MintStatus.DONE -> handleDoneStatus(transactionHash, secretKey, response)
        }
    }

    private fun handleDoneStatus(
        transactionHash: String,
        secretKey: ByteArray,
        response: ResponseQueryTxMint
    ) {
        setStatus(RenTransactionStatus.Minting(transactionHash))
        try {
            val signature = lockAndMint.mint(Account(secretKey))
            Timber.tag(REN_TAG).d("Mint signature received: $signature")
        } catch (e: Throwable) {
            Timber.e(e, "Error minting transaction: $transactionHash")
        }
        val amount = response.valueOut.amount.toBigInteger().fromLamports(BTC_DECIMALS)
        setStatus(RenTransactionStatus.SuccessfullyMinted(transactionHash, amount))
    }

    private fun setStatus(status: RenTransactionStatus) {
        val latestStatus = transaction.getLatestStatus()
        if (latestStatus == status) return
        transaction.statuses += status
    }
}
