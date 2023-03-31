package org.p2p.wallet.bridge.send.statemachine.handler.bridge

import java.math.BigDecimal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.p2p.core.token.Token
import org.p2p.core.token.TokenVisibility
import org.p2p.ethereumkit.external.model.ERC20Tokens
import org.p2p.wallet.bridge.send.statemachine.SendActionHandler
import org.p2p.wallet.bridge.send.statemachine.SendFeatureAction
import org.p2p.wallet.bridge.send.statemachine.SendState
import org.p2p.wallet.bridge.send.statemachine.fee.SendBridgeFeeLoader
import org.p2p.wallet.bridge.send.statemachine.model.SendInitialData
import org.p2p.wallet.bridge.send.statemachine.model.SendToken
import org.p2p.wallet.user.interactor.UserInteractor

class InitFeatureActionHandler(
    private val feeLoader: SendBridgeFeeLoader,
    private val initialData: SendInitialData.Bridge,
    private val userInteractor: UserInteractor
) : SendActionHandler {

    private val supportedTokensMints = ERC20Tokens.values().map { it.mintAddress }

    override fun canHandle(
        newEvent: SendFeatureAction,
        staticState: SendState
    ): Boolean = newEvent is SendFeatureAction.InitFeature

    override fun handle(
        lastStaticState: SendState.Static,
        newAction: SendFeatureAction
    ): Flow<SendState> = flow {
        val userTokens = userInteractor.getNonZeroUserTokens()
            .filter { it.mintAddress in supportedTokensMints }
            .ifEmpty {
                // TODO PWN-7613 also block button as we can't send we do not have funds
                val usdCet = userInteractor.findTokenDataByAddress(ERC20Tokens.USDC.mintAddress) as Token.Other
                listOf(toTokenActiveStub(usdCet))
            }

        val isTokenChangeEnabled = userTokens.size > 1
        val initialToken = initialData.initialToken ?: SendToken.Bridge(userTokens.first())

        emit(SendState.Static.Initialize(initialToken, isTokenChangeEnabled))
        val initialState = if (initialData.initialAmount == null) {
            SendState.Static.TokenZero(initialToken, null)
        } else {
            SendState.Static.TokenNotZero(initialToken, initialData.initialAmount)
        }
        emit(initialState)
    }.flatMapMerge { state ->
        if (state !is SendState.Static.Initialize)
            feeLoader.updateFee(state)
        else flowOf(state)
    }

    private fun toTokenActiveStub(token: Token.Other): Token.Active {
        return Token.Active(
            publicKey = token.publicKey.orEmpty(),
            totalInUsd = BigDecimal.ZERO,
            total = BigDecimal.ZERO,
            tokenSymbol = token.tokenSymbol,
            decimals = token.decimals,
            mintAddress = token.mintAddress,
            tokenName = token.tokenName,
            iconUrl = token.iconUrl,
            rate = null,
            visibility = TokenVisibility.DEFAULT,
            serumV3Usdc = token.serumV3Usdc,
            serumV3Usdt = token.serumV3Usdt,
            isWrapped = token.isWrapped
        )
    }
}
