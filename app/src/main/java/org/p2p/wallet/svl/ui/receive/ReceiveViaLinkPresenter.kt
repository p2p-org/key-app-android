package org.p2p.wallet.svl.ui.receive

import android.content.Context
import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.core.token.Token
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.newsend.model.TemporaryAccount
import org.p2p.wallet.svl.interactor.ReceiveViaLinkInteractor
import org.p2p.wallet.svl.interactor.SendViaLinkWrapper
import org.p2p.wallet.svl.model.ReceiveViaLinkMapper
import org.p2p.wallet.svl.model.SendViaLinkClaimingState
import org.p2p.wallet.svl.model.TemporaryAccountState
import org.p2p.wallet.svl.ui.error.SendViaLinkError
import org.p2p.wallet.updates.ConnectionStateProvider
import org.p2p.wallet.utils.emptyString

class ReceiveViaLinkPresenter(
    private val context: Context,
    private val receiveViaLinkInteractor: ReceiveViaLinkInteractor,
    private val receiveViaLinkMapper: ReceiveViaLinkMapper,
    private val connectionStateProvider: ConnectionStateProvider
) : BasePresenter<ReceiveViaLinkContract.View>(),
    ReceiveViaLinkContract.Presenter {

    override fun claimToken(temporaryAccount: TemporaryAccount, token: Token.Active) {
        launch {
            try {
                view?.renderState(SendViaLinkClaimingState.ClaimingInProcess)
                receiveViaLinkInteractor.receiveTransfer(temporaryAccount, token)

                val successMessage = receiveViaLinkMapper.mapClaimSuccessMessage(token)
                val state = SendViaLinkClaimingState.ClaimSuccess(successMessage)
                view?.renderState(state)
            } catch (e: Throwable) {
                Timber.e(e, "Error claiming token")
                view?.renderState(SendViaLinkClaimingState.ClaimFailed(e))
            }
        }
    }

    override fun parseAccountFromLink(link: SendViaLinkWrapper, isRetry: Boolean) {
        if (!isInternetConnectionEnabled()) {
            val state = SendViaLinkClaimingState.ParsingFailed(
                titleRes = R.string.error_no_internet_message_no_emoji,
                subTitleRes = null,
                iconRes = R.drawable.ic_cat
            )
            view?.renderState(state)
            return
        }

        if (isRetry) {
            view?.showButtonLoading(isLoading = true)
        } else {
            view?.renderState(SendViaLinkClaimingState.InitialLoading)
        }
        parseAccount(link)
    }

    private fun parseAccount(link: SendViaLinkWrapper) {
        launch {
            try {
                val state = receiveViaLinkInteractor.parseAccountFromLink(link)
                handleState(state)
            } catch (e: Throwable) {
                Timber.e(e, "Error parsing link")
                view?.renderState(SendViaLinkClaimingState.ParsingFailed())
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
                view?.renderState(SendViaLinkClaimingState.ParsingFailed())
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
}
