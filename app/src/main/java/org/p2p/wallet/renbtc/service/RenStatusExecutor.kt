package org.p2p.wallet.renbtc.service

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.kits.renBridge.LockAndMint
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseQueryTxMint
import org.p2p.wallet.main.model.RenBTCPayment
import org.p2p.wallet.renbtc.RenTransactionManager.Companion.REN_TAG
import org.p2p.wallet.renbtc.model.MintStatus
import org.p2p.wallet.renbtc.model.RenTransactionStatus
import org.p2p.wallet.renbtc.ui.main.BTC_DECIMALS
import org.p2p.wallet.utils.fromLamports
import timber.log.Timber

private const val ONE_MINUTE_DELAY = 1000L * 60L

class RenStatusExecutor(
    private val lockAndMint: LockAndMint,
    private val payment: RenBTCPayment,
    private val secretKey: ByteArray
) : RenTransactionExecutor {

    private val state = MutableStateFlow<MutableList<RenTransactionStatus>>(mutableListOf())

    init {
        setStatus(RenTransactionStatus.WaitingDepositConfirm(payment.transactionHash))
    }

    override fun getTransactionHash(): String = payment.transactionHash

    override suspend fun execute() {
        if (state.value.lastOrNull() !is RenTransactionStatus.WaitingDepositConfirm) return
        lockAndMint.getDepositState(payment.transactionHash, payment.txIndex.toString(), payment.amount.toString())
        val txHash = lockAndMint.submitMintTransaction()
        startQueryMintPolling(txHash, secretKey)
    }

    override fun getStateFlow(): MutableStateFlow<MutableList<RenTransactionStatus>> = state

    private suspend fun startQueryMintPolling(txHash: String, secretKey: ByteArray) {
        while (true) {
            val response = lockAndMint.lockAndMint(txHash)
            handleStatus(response, secretKey)
            delay(ONE_MINUTE_DELAY)
        }
    }

    private fun handleStatus(response: ResponseQueryTxMint, secretKey: ByteArray) {
        val status = response.txStatus
        Timber.tag(REN_TAG)
            .d("Current mint status: $status, will check again in one minute")

        if (!isValidStatus()) return

        when (MintStatus.parse(status)) {
            MintStatus.CONFIRMED -> {
                setStatus(RenTransactionStatus.AwaitingForSignature(payment.transactionHash))
            }
            MintStatus.EXECUTING -> {
                setStatus(RenTransactionStatus.SubmittingToRenVM(payment.transactionHash))
            }
            MintStatus.DONE -> {
                setStatus(RenTransactionStatus.Minting(payment.transactionHash))
                val signature = lockAndMint.mint(Account(secretKey))
                Timber.tag(REN_TAG).d("Mint signature received: $signature")
                val amount = response.valueOut.amount.toBigInteger().fromLamports(BTC_DECIMALS)

                setStatus(RenTransactionStatus.SuccessfullyMinted(payment.transactionHash, amount))
            }
        }
    }

    private fun setStatus(status: RenTransactionStatus) {
        state.value = state.value.toMutableList().also { statuses ->
            val latestStatus = statuses.lastOrNull()
            when {
                latestStatus is RenTransactionStatus.SuccessfullyMinted ->
                    if (status !is RenTransactionStatus.Minting) statuses.add(status)
                latestStatus != status ->
                    statuses.add(status)
            }
        }
    }

    private fun isValidStatus(): Boolean {
        val latestStatus = state.value.lastOrNull()
        if (latestStatus is RenTransactionStatus.Minting ||
            latestStatus is RenTransactionStatus.SuccessfullyMinted
        ) return false

        return true
    }
}