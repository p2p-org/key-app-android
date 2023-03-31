package org.p2p.wallet.svl.ui.receive

import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.core.token.Token
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.newsend.model.TemporaryAccount
import org.p2p.wallet.svl.interactor.ReceiveViaLinkInteractor
import org.p2p.wallet.svl.model.ReceiveViaLinkMapper
import org.p2p.wallet.svl.model.SendViaLinkClaimingState
import org.p2p.wallet.svl.model.TemporaryAccountState
import org.p2p.wallet.svl.ui.error.SendViaLinkError
import org.p2p.wallet.utils.emptyString

class ReceiveViaLinkPresenter(
    private val receiveViaLinkInteractor: ReceiveViaLinkInteractor,
    private val receiveViaLinkMapper: ReceiveViaLinkMapper
) : BasePresenter<ReceiveViaLinkContract.View>(), ReceiveViaLinkContract.Presenter {

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

    override fun handleState(state: TemporaryAccountState) {
        Timber.i("Handling receive via link state: ${state::class.simpleName}")
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
                    tokenIcon = receiveViaLinkMapper.mapTokenIcon(state.token),
                )
            }
            is TemporaryAccountState.EmptyBalance -> {
                view?.navigateToErrorScreen(SendViaLinkError.ALREADY_CLAIMED)
            }
            is TemporaryAccountState.ParsingFailed -> {
                view?.renderState(SendViaLinkClaimingState.ParsingFailed)
            }
            TemporaryAccountState.BrokenLink -> Unit
        }
    }
}
