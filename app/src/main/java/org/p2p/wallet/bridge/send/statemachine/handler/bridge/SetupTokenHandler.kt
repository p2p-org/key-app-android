package org.p2p.wallet.bridge.send.statemachine.handler.bridge

import timber.log.Timber
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.p2p.wallet.R
import org.p2p.wallet.bridge.send.statemachine.SendActionHandler
import org.p2p.wallet.bridge.send.statemachine.SendFeatureAction
import org.p2p.wallet.bridge.send.statemachine.SendState
import org.p2p.wallet.user.interactor.UserInteractor

class SetupTokenHandler(
    private val userInteractor: UserInteractor
) : SendActionHandler {

    override fun canHandle(newEvent: SendFeatureAction): Boolean {
        return newEvent is SendFeatureAction.SetupInitialToken
    }

    override fun handle(newAction: SendFeatureAction): Flow<SendState> = flow {
        val action = newAction as SendFeatureAction.SetupInitialToken
        val userTokens = userInteractor.getNonZeroUserTokens()
        if (userTokens.isEmpty()) {
            val state = SendState.Exception.SnackbarMessage(R.string.error_general_message)
            emit(state)
            return@flow
        }

        val isTokenChangeEnabled = userTokens.size > 1 && action.initialToken == null
        val token = action.initialToken ?: userTokens.first()
        val solToken = if (token.isSOL) token else userInteractor.getUserSolToken()
        if (solToken == null) {
            val state = SendState.Exception.SnackbarMessage(R.string.error_general_message)
            Timber.e(IllegalStateException("Couldn't find user's SOL account!"))
            emit(state)
            return@flow
        }
    }
}
