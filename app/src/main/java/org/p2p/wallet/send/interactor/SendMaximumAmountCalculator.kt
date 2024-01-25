package org.p2p.wallet.send.interactor

import timber.log.Timber
import java.math.BigDecimal
import org.p2p.core.crypto.Base58String
import org.p2p.core.token.Token
import org.p2p.core.utils.toPowerValue
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.send.repository.SendServiceRepository

/**
 * Special entity that encapsulates logic that is connected to
 * calculating maximum amount that is available to send
 *
 * the problem is that we a user decides to send 5 BERN he can't because of fees that
 * are applied on top.
 * So that's why we are using our backend service and cached responses for such cases
 * we ask backend totalAmount with all the fees and use it here
 *
 * add more additional logic and calculations as needed
 */
class SendMaximumAmountCalculator(
    private val sendServiceRepository: SendServiceRepository,
    private val tokenKeyProvider: TokenKeyProvider
) {
    suspend fun getMaxAvailableAmountToSend(
        token: Token.Active,
        recipient: Base58String
    ): BigDecimal? = kotlin.runCatching {
        // user can send full amount of SOL
        if (token.isSOL) {
            return@runCatching token.totalInLamports
        }

        sendServiceRepository.getMaxAmountToSend(
            userWallet = tokenKeyProvider.publicKeyBase58,
            recipient = recipient,
            token = token
        )
    }
        // not fromLamports because we can lose some numbers in the process
        .map { it.toBigDecimal().divide(token.decimals.toPowerValue()) }
        .onFailure { Timber.i(it) }
        .getOrNull()
}
