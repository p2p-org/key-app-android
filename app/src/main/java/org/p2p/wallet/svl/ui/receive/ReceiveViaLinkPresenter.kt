package org.p2p.wallet.svl.ui.receive

import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import java.net.UnknownHostException
import kotlinx.coroutines.launch
import org.p2p.core.token.Token
import org.p2p.solanaj.core.PublicKey
import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.history.model.rpc.RpcHistoryAmount
import org.p2p.wallet.history.model.rpc.RpcHistoryTransaction
import org.p2p.wallet.history.model.rpc.RpcHistoryTransactionType
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.newsend.model.TemporaryAccount
import org.p2p.wallet.svl.interactor.ReceiveViaLinkError
import org.p2p.wallet.svl.interactor.ReceiveViaLinkInteractor
import org.p2p.wallet.svl.interactor.SendViaLinkWrapper
import org.p2p.wallet.svl.model.ReceiveViaLinkMapper
import org.p2p.wallet.svl.model.SendViaLinkClaimingState
import org.p2p.wallet.svl.model.TemporaryAccountState
import org.p2p.wallet.svl.ui.error.SendViaLinkError
import org.p2p.wallet.transaction.model.HistoryTransactionStatus
import org.p2p.wallet.updates.NetworkConnectionStateProvider
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.toBase58Instance
import org.p2p.wallet.utils.toPublicKey

class ReceiveViaLinkPresenter(
    private val receiveViaLinkInteractor: ReceiveViaLinkInteractor,
    private val receiveViaLinkMapper: ReceiveViaLinkMapper,
    private val historyInteractor: HistoryInteractor,
    private val connectionStateProvider: NetworkConnectionStateProvider,
    private val tokenKeyProvider: TokenKeyProvider,
    private val usernameInteractor: UsernameInteractor,
    private val appScope: AppScope
) : BasePresenter<ReceiveViaLinkContract.View>(),
    ReceiveViaLinkContract.Presenter {

    override fun claimToken(temporaryAccount: TemporaryAccount, token: Token.Active) {
        appScope.launch {
            try {
                view?.renderState(SendViaLinkClaimingState.ClaimingInProcess)

                val recipient = tokenKeyProvider.publicKey.toPublicKey()
                val transactionId = receiveViaLinkInteractor.receiveTransfer(temporaryAccount, token, recipient)

                historyInteractor.addPendingTransaction(
                    txSignature = transactionId,
                    mintAddress = token.mintAddress.toBase58Instance(),
                    transaction = buildPendingTransaction(
                        transactionId = transactionId,
                        sender = temporaryAccount,
                        token = token,
                        recipient = recipient
                    )
                )

                val successMessage = receiveViaLinkMapper.mapClaimSuccessMessage(token)
                val state = SendViaLinkClaimingState.ClaimSuccess(successMessage)
                view?.renderState(state)
            } catch (e: Throwable) {
                Timber.e(ReceiveViaLinkError("Error claiming token", e))
                val textRes = if (e is UnknownHostException) {
                    R.string.transaction_description_internet_error
                } else {
                    R.string.transaction_description_svl_failed
                }
                view?.renderState(SendViaLinkClaimingState.ClaimFailed(textRes))
            }
        }
    }

    override fun parseAccountFromLink(link: SendViaLinkWrapper, isRetry: Boolean) {
        if (!isInternetConnectionEnabled()) {
            view?.renderState(SendViaLinkClaimingState.ParsingFailed.buildInternetError())
            return
        }

        parseAccount(link, isRetry)
    }

    private fun parseAccount(link: SendViaLinkWrapper, isRetry: Boolean) {
        if (isRetry) {
            view?.showButtonLoading(isLoading = true)
        } else {
            view?.renderState(SendViaLinkClaimingState.InitialLoading)
        }
        launch {
            try {
                val state = receiveViaLinkInteractor.parseAccountFromLink(link)
                handleState(state)
            } catch (e: Throwable) {
                Timber.e(ReceiveViaLinkError("Error parsing link", e))
                view?.renderState(SendViaLinkClaimingState.ParsingFailed.buildUnknownError())
            } finally {
                view?.showButtonLoading(isLoading = false)
            }
        }
    }

    private fun handleState(state: TemporaryAccountState) {
        when (state) {
            is TemporaryAccountState.Active -> {
                val claimState = SendViaLinkClaimingState.ReadyToClaim(
                    temporaryAccount = state.account,
                    token = state.token
                )
                view?.renderState(claimState)
                view?.renderClaimTokenDetails(
                    tokenAmount = receiveViaLinkMapper.mapTokenAmount(state.token),
                    // TODO: get address from account
                    sentFromAddress = receiveViaLinkMapper.mapSenderAddress(emptyString()),
                    tokenIcon = receiveViaLinkMapper.mapTokenIcon(state.token)
                )
            }
            is TemporaryAccountState.ParsingFailed -> {
                view?.renderState(SendViaLinkClaimingState.ParsingFailed.buildUnknownError())
            }
            is TemporaryAccountState.EmptyBalance -> {
                view?.showLinkError(SendViaLinkError.ALREADY_CLAIMED)
            }
            is TemporaryAccountState.BrokenLink -> {
                view?.showLinkError(SendViaLinkError.BROKEN_LINK)
            }
        }
    }

    private fun isInternetConnectionEnabled(): Boolean =
        connectionStateProvider.hasConnection()

    private fun buildPendingTransaction(
        transactionId: String,
        sender: TemporaryAccount,
        token: Token.Active,
        recipient: PublicKey
    ): RpcHistoryTransaction.Transfer =
        RpcHistoryTransaction.Transfer(
            signature = transactionId,
            date = ZonedDateTime.now(),
            blockNumber = -1,
            type = RpcHistoryTransactionType.RECEIVE,
            senderAddress = sender.address,
            amount = RpcHistoryAmount(token.total, token.totalInUsdScaled),
            destination = recipient.toBase58(),
            counterPartyUsername = usernameInteractor.getUsername()?.fullUsername,
            fees = null,
            status = HistoryTransactionStatus.PENDING,
            iconUrl = token.iconUrl,
            symbol = token.tokenSymbol
        )
}
