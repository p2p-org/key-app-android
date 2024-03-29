package org.p2p.wallet.history.ui.detailsbottomsheet

import android.content.res.Resources
import org.threeten.bp.format.DateTimeFormatter
import timber.log.Timber
import java.util.Locale
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.utils.emptyString
import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.auth.username.repository.model.UsernameDetails
import org.p2p.wallet.common.date.toDateString
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.model.rpc.RpcHistoryTransaction
import org.p2p.wallet.transaction.model.HistoryTransactionStatus
import org.p2p.wallet.utils.DateTimeUtils
import org.p2p.wallet.utils.cutMiddle

class HistoryTransactionDetailsPresenter(
    private val resources: Resources,
    private val historyInteractor: HistoryInteractor,
    private val usernameInteractor: UsernameInteractor,
) : BasePresenter<HistoryTransactionDetailsContract.View>(),
    HistoryTransactionDetailsContract.Presenter {

    private val timeFormat = DateTimeFormatter.ofPattern(DateTimeUtils.PATTERN_FULL_DAY, Locale.US)
    private var tx: HistoryTransaction? = null

    override fun load(transactionId: String) {
        launch {
            try {
                view?.showLoading(isLoading = true)
                Timber.d("Start finding tx by id: $transactionId")
                tx = historyInteractor.findTransactionById(transactionId)
                Timber.d("Tx by id is found")
                loadDetailsByTransaction(tx)
            } catch (e: Throwable) {
                Timber.e(e, "Error on finding transaction by id: $e")
                view?.showErrorMessage(e)
            } finally {
                view?.showLoading(isLoading = false)
            }
        }
    }

    private suspend fun loadDetailsByTransaction(transaction: HistoryTransaction?) {
        if (transaction == null) {
            view?.showError(R.string.details_transaction_not_found)
            return
        }

        val transactionDate = resources.getString(
            R.string.transaction_details_date_format,
            transaction.date.toDateString(resources),
            transaction.date.format(timeFormat)
        )
        view?.showSubtitle(transactionDate)

        showStatusAndSignature(transaction as? RpcHistoryTransaction)
        when (transaction) {
            is RpcHistoryTransaction.Swap -> parseSwap(transaction)
            is RpcHistoryTransaction.Transfer -> parseTransfer(transaction)
            is RpcHistoryTransaction.ReferralReward -> parseReferralReward(transaction)
            is RpcHistoryTransaction.BurnOrMint -> parseBurnOrMint(transaction)
            is RpcHistoryTransaction.WormholeReceive -> parseWormholeReceive(transaction)
            is RpcHistoryTransaction.WormholeSend -> parseWormholeSend(transaction)
            is RpcHistoryTransaction.CreateAccount -> parseCreateAccount(transaction)
            is RpcHistoryTransaction.CloseAccount -> parseCloseAccount(transaction)
            is RpcHistoryTransaction.StakeUnstake -> parseStakeUnstake(transaction)
            is RpcHistoryTransaction.Unknown -> parseUnknown(transaction)
            // TODO PWN-5813 add other states !
            else -> {
                Timber.e("Unsupported transaction: $transaction")
                view?.showError(R.string.details_transaction_not_found)
            }
        }
    }

    private fun parseSwap(transaction: RpcHistoryTransaction.Swap) {
        view?.apply {
            val usdTotal = transaction.getTokenBUsdAmount()
            val total = transaction.getFormattedAmountWithArrow()
            showAmount(amountToken = total, amountUsd = usdTotal)
            if (!transaction.status.isPending()) {
                showFee(transaction.fees)
            }

            val sourceIcon = transaction.tokenA.logoUrl
            val destinationIcon = transaction.tokenB.logoUrl
            if (sourceIcon.isNullOrEmpty() && destinationIcon.isNullOrEmpty()) {
                showTransferView(null, R.drawable.ic_swap_arrows)
            } else {
                showSwapView(sourceIcon, destinationIcon)
            }
            hideSendReceiveTitleAndValue()
        }
    }

    private suspend fun parseTransfer(transaction: RpcHistoryTransaction.Transfer) {
        view?.apply {
            if (transaction.status.isPending()) {
                showSubtitle(resources.getString(R.string.details_pending))
                showProgressTransactionInProgress()
            }
            showTransferView(transaction.token.logoUrl, transaction.getIcon())
            if (!transaction.status.isPending()) {
                showFee(transaction.fees)
            }
            showAmount(
                amountToken = transaction.getFormattedTotal(),
                amountUsd = transaction.getFormattedAmountUsd()
            )
            showTransferAddress(
                isSend = transaction.isSend,
                senderAddress = transaction.senderAddress,
                receiverAddress = transaction.destination,
                isPending = transaction.status.isPending()
            )
        }
    }

    private suspend fun parseReferralReward(transaction: RpcHistoryTransaction.ReferralReward) {
        val transferActorAddress = transaction.senderAddress.toBase58Instance()

        view?.apply {
            setSmokeBackground()
            setTitle(R.string.transaction_details_referral_title)
            showNewButtons(
                firstButtonTitleRes = R.string.transaction_details_referral_button_first,
                secondButtonTitleRes = R.string.transaction_details_referral_button_second
            )
            showTransferView(
                tokenIconUrl = transaction.iconUrl,
                placeholderIcon = transaction.getIcon()
            )
            showAmountReferralReward(amountToken = transaction.getFormattedTotal())

            showSenderAddress(
                senderAddress = transferActorAddress,
                senderUsername = null,
                isReceivePending = true
            )
            playApplauseAnimation()
        }

        // fetching username is failing all the time and it takes way too long
        // so:
        //  1. defer it,
        //  2. limit by timeout
        //  todo: the same ought to be done for [parseTransfer]
        async {
            Timber.d("Start loading username")
            val transferActorUsername = withTimeoutOrNull(5.seconds) {
                getTransferActorUsername(transferActorAddress)
            }

            if (transferActorUsername != null && view != null) {
                transferActorUsername.username.fullUsername.let {
                    view?.showSenderAddress(
                        senderAddress = transferActorAddress,
                        senderUsername = it,
                        isReceivePending = true
                    )
                }
            } else {
                Timber.w("Couldn't load username for address: $transferActorAddress")
            }
        }
    }

    private suspend fun showTransferAddress(
        isSend: Boolean,
        isPending: Boolean,
        senderAddress: String,
        receiverAddress: String,
        toEth: Boolean = false
    ) {
        val transferActorAddress = if (isSend) receiverAddress.toBase58Instance() else senderAddress.toBase58Instance()
        val transferActorUsername: UsernameDetails? = getTransferActorUsername(transferActorAddress)
        if (isSend) {
            view?.showReceiverAddress(
                receiverAddress = transferActorAddress,
                receiverUsername = transferActorUsername?.username?.fullUsername,
                toEth = toEth
            )
        } else {
            view?.showSenderAddress(
                senderAddress = transferActorAddress,
                senderUsername = transferActorUsername?.username?.fullUsername,
                isReceivePending = isPending
            )
        }
    }

    private suspend fun getTransferActorUsername(actorAddress: Base58String): UsernameDetails? {
        return runCatching { usernameInteractor.findUsernameByAddress(actorAddress) }
            .onFailure {
                if (it !is CancellationException) {
                    Timber.e(it, "Failed to find username by address: $actorAddress")
                }
            }
            .getOrNull()
    }

    private fun parseBurnOrMint(transaction: RpcHistoryTransaction.BurnOrMint) {
        view?.apply {
            val usdTotal = transaction.getFormattedAmountUsd()
            val total = transaction.getFormattedAbsTotal()
            showAmount(total, usdTotal)
            if (!transaction.status.isPending()) {
                showFee(transaction.fees)
            }
            showTransferView(transaction.token.logoUrl, R.drawable.ic_placeholder_v2)
            showStateTitleValue(
                resources.getString(
                    if (transaction.isBurn) R.string.transaction_details_burn
                    else R.string.transaction_details_mint
                ),
                transaction.signature.cutMiddle()
            )
        }
    }

    private suspend fun parseWormholeReceive(transaction: RpcHistoryTransaction.WormholeReceive) {
        view?.apply {
            if (transaction.status.isPending()) {
                view?.showSubtitle(resources.getString(R.string.details_pending))
                showProgressTransactionInProgress()
            }
            showTransferView(transaction.token.logoUrl, R.drawable.ic_placeholder_v2)
            if (transaction.fees != null && !transaction.status.isPending()) {
                showFee(transaction.fees)
            }
            showAmount(
                amountToken = transaction.getFormattedTotal(),
                amountUsd = transaction.getFormattedUsdAmount()
            )
            showTransferAddress(
                isSend = false,
                senderAddress = resources.getString(R.string.bridge_details_receive),
                receiverAddress = emptyString(),
                isPending = transaction.status.isPending(),
                toEth = true
            )
        }
    }

    private suspend fun parseWormholeSend(transaction: RpcHistoryTransaction.WormholeSend) {
        view?.apply {
            if (transaction.status.isPending()) {
                view?.showSubtitle(resources.getString(R.string.details_pending))
                showProgressTransactionInProgress()
            }
            showTransferView(transaction.token.logoUrl, R.drawable.ic_placeholder_v2)
            if (transaction.fees != null && !transaction.status.isPending()) {
                showFee(transaction.fees)
            }
            showAmount(
                amountToken = transaction.getFormattedTotal(),
                amountUsd = transaction.getFormattedUsdAmount()
            )
            showTransferAddress(
                isSend = true,
                senderAddress = emptyString(),
                receiverAddress = transaction.sourceAddress,
                isPending = transaction.status.isPending(),
                toEth = true
            )
        }
    }

    private fun parseCreateAccount(transaction: RpcHistoryTransaction.CreateAccount) {
        view?.apply {
            val usdTotal = transaction.getFormattedAmountUsd()
            val total = transaction.getFormattedTotal()
            showAmount(total, usdTotal)
            showFee(transaction.fees)
            showTransferView(transaction.token.logoUrl, R.drawable.ic_transaction_create)
            showStateTitleValue(
                resources.getString(R.string.transaction_details_signature),
                transaction.signature.cutMiddle()
            )
        }
    }

    private fun parseCloseAccount(transaction: RpcHistoryTransaction.CloseAccount) {
        view?.apply {
            showAmount(resources.getString(R.string.transaction_details_no_balance_change), amountUsd = null)
            showFee(transaction.fees)
            showTransferView(transaction.iconUrl, R.drawable.ic_transaction_closed)
            showStateTitleValue(
                title = resources.getString(R.string.transaction_details_signature),
                value = transaction.signature.cutMiddle()
            )
        }
    }

    private fun parseStakeUnstake(transaction: RpcHistoryTransaction.StakeUnstake) {
        view?.apply {
            val usdTotal = transaction.getFormattedAmountUsd()
            val total = transaction.getFormattedTotal()
            showAmount(total, usdTotal)
            showFee(transaction.fees)
            showTransferView(transaction.token.logoUrl, transaction.getIcon())
            showStateTitleValue(
                title = resources.getString(
                    if (transaction.isStake) {
                        R.string.transaction_details_stake
                    } else {
                        R.string.transaction_details_unstake
                    }
                ),
                value = transaction.signature.cutMiddle()
            )
        }
    }

    private fun parseUnknown(transaction: RpcHistoryTransaction.Unknown) {
        view?.apply {
            val usdTotal = transaction.getFormattedAmountUsd()
            val total = transaction.getFormattedTotal()
            showAmount(total, usdTotal)
            showTransferView(tokenIconUrl = null, placeholderIcon = R.drawable.ic_transaction_unknown)
            showStateTitleValue(
                title = resources.getString(R.string.transaction_details_signature),
                value = transaction.signature.cutMiddle(cutCount = 8)
            )
        }
    }

    private fun showStatusAndSignature(transaction: RpcHistoryTransaction?) {
        val status = transaction?.status ?: return
        val colorRes: Int
        val titleRes: Int
        when (status) {
            HistoryTransactionStatus.COMPLETED -> {
                titleRes = R.string.transaction_details_title_succeeded
                val isTransactionTransfer = transaction is RpcHistoryTransaction.Transfer && transaction.isSend
                val isTransactionBurn = transaction is RpcHistoryTransaction.BurnOrMint && transaction.isBurn
                val isTransactionSwap = transaction is RpcHistoryTransaction.Swap
                val isWormholeSend = transaction is RpcHistoryTransaction.WormholeSend
                val conditionsList = listOf(
                    isTransactionTransfer,
                    isTransactionBurn,
                    isTransactionSwap,
                    isWormholeSend
                )
                colorRes = if (conditionsList.any { it }) {
                    R.color.text_night
                } else {
                    R.color.text_mint
                }
            }
            HistoryTransactionStatus.PENDING -> {
                titleRes = R.string.transaction_details_title_submitted
                colorRes = R.color.text_sun
            }
            HistoryTransactionStatus.ERROR -> {
                titleRes = R.string.transaction_details_title_failed
                colorRes = R.color.text_rose
                showErrorState(transaction)
            }
        }
        view?.apply {
            showStatus(titleRes, colorRes)
            showTransactionId(transaction.signature)
        }
    }

    private fun showErrorState(transaction: RpcHistoryTransaction) {
        when (transaction) {
            is RpcHistoryTransaction.Swap -> resources.getString(transaction.getTypeName())
            is RpcHistoryTransaction.Transfer -> resources.getString(transaction.getTypeName())
            is RpcHistoryTransaction.BurnOrMint -> resources.getString(transaction.getTitle())
            is RpcHistoryTransaction.StakeUnstake -> resources.getString(transaction.getTypeName())
            is RpcHistoryTransaction.Unknown -> resources.getString(R.string.transaction_history_unknown)
            else -> null
        }?.also { errorTypeName ->
            view?.showProgressTransactionErrorState(
                resources.getString(R.string.transaction_details_status_message_format, errorTypeName)
            )
        }
    }

    override fun onPrimaryButtonClick() {
        when (tx) {
            is RpcHistoryTransaction.ReferralReward -> {
                view?.dismiss()
            }
            else -> Unit
        }
    }

    override fun onSecondaryButtonClick() {
        when (tx) {
            is RpcHistoryTransaction.ReferralReward -> {
                view?.navigateToSolscan(tx!!.getHistoryTransactionId())
            }
            else -> Unit
        }
    }
}
