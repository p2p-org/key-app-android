package org.p2p.wallet.svl.interactor

import java.math.BigInteger
import org.p2p.core.token.Token
import org.p2p.core.utils.isMoreThan
import org.p2p.solanaj.core.Account
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.home.model.TokenConverter
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.newsend.model.SEND_LINK_FORMAT
import org.p2p.wallet.newsend.model.TemporaryAccount
import org.p2p.wallet.rpc.repository.account.RpcAccountRepository
import org.p2p.wallet.svl.model.SendLinkGenerator
import org.p2p.wallet.svl.model.TemporaryAccountState
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.utils.toPublicKey

class ReceiveViaLinkInteractor(
    private val rpcAccountRepository: RpcAccountRepository,
    private val userLocalRepository: UserLocalRepository,
    private val sendViaLinkInteractor: SendViaLinkInteractor,
    private val tokenKeyProvider: TokenKeyProvider
) {

    suspend fun parseAccountFromLink(link: SendViaLinkWrapper): TemporaryAccountState {
        val seedCode = link.link.substringAfterLast(SEND_LINK_FORMAT).toList().map { it.toString() }

        if (!SendLinkGenerator.isValidSeedCode(seedCode)) {
            return TemporaryAccountState.BrokenLink
        }

        val temporaryAccount = try {
            SendLinkGenerator.parseTemporaryAccount(seedCode)
        } catch (e: Throwable) {
            return TemporaryAccountState.BrokenLink
        }

        val tokenAccounts = rpcAccountRepository.getTokenAccountsByOwner(temporaryAccount.publicKey)
        val activeAccount = tokenAccounts.accounts.firstOrNull {
            it.account.data.parsed.info.tokenAmount.amount.toBigInteger().isMoreThan(BigInteger.ZERO)
        } ?: return TemporaryAccountState.EmptyBalance

        val info = activeAccount.account.data.parsed.info
        val tokenData = userLocalRepository.findTokenData(info.mint) ?: return TemporaryAccountState.ParsingFailed

        val token = TokenConverter.fromNetwork(
            account = activeAccount,
            tokenData = tokenData,
            price = null
        )
        return TemporaryAccountState.Active(
            account = temporaryAccount,
            token = token
        )
    }

    suspend fun receiveTransfer(temporaryAccount: TemporaryAccount, token: Token.Active) {
        val recipient = tokenKeyProvider.publicKey.toPublicKey()
        val senderAccount = Account(temporaryAccount.keypair)

        sendViaLinkInteractor.sendTransaction(
            senderAccount = senderAccount,
            destinationAddress = recipient,
            token = token,
            lamports = token.totalInLamports,
            memo = BuildConfig.sendViaLinkMemo,
            isSimulation = false
        )
    }
}
