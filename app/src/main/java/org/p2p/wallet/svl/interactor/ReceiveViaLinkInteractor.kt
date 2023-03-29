package org.p2p.wallet.svl.interactor

import java.math.BigInteger
import org.p2p.core.utils.isMoreThan
import org.p2p.wallet.newsend.model.TemporaryAccount
import org.p2p.wallet.rpc.repository.account.RpcAccountRepository
import org.p2p.wallet.svl.model.SendLinkGenerator
import org.p2p.wallet.svl.model.TemporaryAccountState
import org.p2p.wallet.user.repository.UserLocalRepository

class ReceiveViaLinkInteractor(
    private val rpcAccountRepository: RpcAccountRepository,
    private val userLocalRepository: UserLocalRepository
) {

    suspend fun parseLink(link: SendViaLinkWrapper): TemporaryAccountState {
        val temporaryAccount = SendLinkGenerator.parseTemporaryAccount(link)

        val tokenAccounts = rpcAccountRepository.getTokenAccountsByOwner(temporaryAccount.publicKey)
        val activeAccount = tokenAccounts.accounts.firstOrNull {
            it.account.data.parsed.info.tokenAmount.amount.toBigInteger().isMoreThan(BigInteger.ZERO)
        } ?: return TemporaryAccountState.EmptyBalance

        val info = activeAccount.account.data.parsed.info
        val tokenData = userLocalRepository.findTokenData(info.mint) ?: return TemporaryAccountState.ParsingFailed

        return TemporaryAccountState.Active(
            account = temporaryAccount,
            amountInLamports = info.tokenAmount.amount.toBigInteger(),
            tokenSymbol = tokenData.symbol,
            tokenDecimals = tokenData.decimals,
            tokenIconUrl = tokenData.iconUrl
        )
    }

    suspend fun receiveTransfer(temporaryAccount: TemporaryAccount) {
    }
}
