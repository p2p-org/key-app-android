package org.p2p.wallet.bridge.send.interactor

import java.math.BigDecimal
import org.p2p.core.token.SolAddress
import org.p2p.core.token.Token
import org.p2p.core.token.TokenVisibility
import org.p2p.ethereumkit.external.model.ERC20Tokens
import org.p2p.ethereumkit.external.repository.EthereumRepository
import org.p2p.wallet.bridge.send.model.BridgeSendFees
import org.p2p.wallet.bridge.send.repository.EthereumSendRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.user.interactor.UserInteractor

class EthereumSendInteractor(
    private val ethereumSendRepository: EthereumSendRepository,
    private val ethereumRepository: EthereumRepository,
    private val userInteractor: UserInteractor,
    private val tokenKeyProvider: TokenKeyProvider
) {

    private val supportedTokensMints = ERC20Tokens.values().map { it.mintAddress }

    suspend fun getSendFee(
        sendTokenMint: SolAddress?,
        amount: String
    ): BridgeSendFees {
        val userAddress = SolAddress(tokenKeyProvider.publicKey)
        val ethereumAddress = ethereumRepository.getAddress()
        return ethereumSendRepository.getSendFee(userAddress, ethereumAddress, sendTokenMint, amount)
    }

    suspend fun supportedSendTokens(): List<Token.Active> {
        return userInteractor.getNonZeroUserTokens()
            .filter { it.mintAddress in supportedTokensMints }
            .ifEmpty {
                // TODO PWN-7613 also block button as we can't send we do not have funds
                val usdCet = userInteractor.findTokenDataByAddress(ERC20Tokens.USDC.mintAddress) as Token.Other
                listOf(toTokenActiveStub(usdCet))
            }
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
            coingeckoId = null,
            rate = null,
            visibility = TokenVisibility.DEFAULT,
            serumV3Usdc = token.serumV3Usdc,
            serumV3Usdt = token.serumV3Usdt,
            isWrapped = token.isWrapped
        )
    }
}
