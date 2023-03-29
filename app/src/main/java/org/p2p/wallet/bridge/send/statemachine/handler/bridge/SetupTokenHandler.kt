package org.p2p.wallet.bridge.send.statemachine.handler.bridge

import timber.log.Timber
import org.p2p.core.token.Token
import org.p2p.ethereumkit.external.model.ERC20Tokens
import org.p2p.wallet.R
import org.p2p.wallet.bridge.send.mapper.SendUiMapper
import org.p2p.wallet.bridge.send.statemachine.SendActionHandler
import org.p2p.wallet.bridge.send.statemachine.SendFeatureAction
import org.p2p.wallet.bridge.send.statemachine.SendState
import org.p2p.wallet.user.interactor.UserInteractor

class SetupTokenHandler(
    private val userInteractor: UserInteractor,
    private val sendUiMapper: SendUiMapper,
) : SendActionHandler {

    private val supportedTokensMints = ERC20Tokens.values().map { it.mintAddress }

    override fun canHandle(newEvent: SendFeatureAction): Boolean {
        return newEvent is SendFeatureAction.SetupInitialToken
    }

    override suspend fun handle(newAction: SendFeatureAction): SendState {

        val action = newAction as SendFeatureAction.SetupInitialToken
        val userTokens = userInteractor.getNonZeroUserTokens()
            .filter { it.mintAddress in supportedTokensMints }
            .ifEmpty {
                // TODO PWN-7613 also block button as we can't send we do not have funds
                val usdCet = userInteractor.findTokenDataByAddress(ERC20Tokens.USDC.mintAddress) as Token.Other
                listOf(sendUiMapper.toTokenActiveStub(usdCet))
            }

        if (userTokens.isEmpty()) {
            val state = SendState.Exception.SnackbarMessage(R.string.error_general_message)
            return state
        }

        val isTokenChangeEnabled = userTokens.size > 1 && action.initialToken == null
        val token = action.initialToken ?: userTokens.first()
        val solToken = if (token.isSOL) token else userInteractor.getUserSolToken()
        if (solToken == null) {
            val state = SendState.Exception.SnackbarMessage(R.string.error_general_message)
            Timber.e(IllegalStateException("Couldn't find user's SOL account!"))
            return state
        }
        return SendState.Event.SetupDefaultFields(token, solToken, isTokenChangeEnabled)
    }
}
