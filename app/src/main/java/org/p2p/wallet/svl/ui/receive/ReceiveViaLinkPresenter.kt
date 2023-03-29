package org.p2p.wallet.svl.ui.receive

import android.content.Context
import timber.log.Timber
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.newsend.model.TemporaryAccount
import org.p2p.wallet.svl.interactor.ReceiveViaLinkInteractor
import org.p2p.wallet.svl.interactor.SendViaLinkWrapper
import org.p2p.wallet.svl.model.SendViaLinkClaimingState
import org.p2p.wallet.svl.model.TemporaryAccountState
import org.p2p.wallet.svl.ui.error.SendViaLinkError
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.DateTimeUtils
import org.p2p.wallet.utils.emptyString

class ReceiveViaLinkPresenter(
    private val context: Context,
    private val receiveViaLinkInteractor: ReceiveViaLinkInteractor
) : BasePresenter<ReceiveViaLinkContract.View>(),
    ReceiveViaLinkContract.Presenter {

    private var parseJob: Job? = null

    override fun claimToken(temporaryAccount: TemporaryAccount, amountInToken: String, tokenSymbol: String) {
        launch {
            try {
                view?.renderState(SendViaLinkClaimingState.ClaimingInProcess)
                receiveViaLinkInteractor.receiveTransfer(temporaryAccount)

                val state = SendViaLinkClaimingState.ClaimSuccess(amountInToken, tokenSymbol)
                view?.renderState(state)
            } catch (e: Throwable) {
                Timber.e(e, "Error claiming token")
                view?.renderState(SendViaLinkClaimingState.ClaimFailed(e))
            }
        }
    }

    override fun parseLink(link: SendViaLinkWrapper) {
        parseJob?.cancel()
        parseJob = launch {
            try {
                val state = receiveViaLinkInteractor.parseLink(link)
                handleState(state)
            } catch (e: Throwable) {
                Timber.e(e, "Error parsing link")
                view?.renderState(SendViaLinkClaimingState.ParsingFailed)
            }
        }
    }

    private fun handleState(state: TemporaryAccountState) {
        when (state) {
            is TemporaryAccountState.Active -> {
                val claimState = SendViaLinkClaimingState.ReadyToClaim(
                    temporaryAccount = state.account,
                    amountInTokens = state.amountInTokens,
                    tokenSymbol = state.tokenSymbol
                )
                view?.renderState(claimState)
                view?.renderClaimTokenDetails(
                    amountInTokens = state.amountInTokens,
                    tokenSymbol = state.tokenSymbol,
                    // TODO: get address from account
                    sentFromAddress = Base58String(emptyString()),
                    tokenIconUrl = state.tokenIconUrl.orEmpty(),
                    linkCreationDate = DateTimeUtils.getDateFormatted(System.currentTimeMillis(), context)
                )
            }
            is TemporaryAccountState.EmptyBalance -> {
                view?.navigateToErrorScreen(SendViaLinkError.ALREADY_CLAIMED)
            }
            is TemporaryAccountState.ParsingFailed -> {
                view?.renderState(SendViaLinkClaimingState.ParsingFailed)
            }
        }
    }
}
