package org.p2p.wallet.svl.interactor

import timber.log.Timber
import java.math.BigInteger
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants.USD_READABLE_SYMBOL
import org.p2p.core.utils.Constants.WRAPPED_SOL_MINT
import org.p2p.core.utils.isMoreThan
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.home.model.TokenConverter
import org.p2p.wallet.home.model.TokenPrice
import org.p2p.wallet.newsend.model.SEND_LINK_FORMAT
import org.p2p.wallet.newsend.model.TemporaryAccount
import org.p2p.wallet.rpc.repository.account.RpcAccountRepository
import org.p2p.wallet.rpc.repository.balance.RpcBalanceRepository
import org.p2p.wallet.svl.model.SendLinkGenerator
import org.p2p.wallet.svl.model.TemporaryAccountState
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.user.repository.prices.TokenId
import org.p2p.wallet.user.repository.prices.TokenPricesRemoteRepository

class ReceiveViaLinkError(
    override val message: String,
    override val cause: Throwable? = null
) : Throwable()

class ReceiveViaLinkInteractor(
    private val rpcAccountRepository: RpcAccountRepository,
    private val rpcBalanceRepository: RpcBalanceRepository,
    private val userLocalRepository: UserLocalRepository,
    private val sendViaLinkInteractor: SendViaLinkInteractor,
    private val tokenPricesRemoteRepository: TokenPricesRemoteRepository
) {

    suspend fun parseAccountFromLink(link: SendViaLinkWrapper): TemporaryAccountState {
        val seedCode = link.link.substringAfterLast(SEND_LINK_FORMAT)
            .toList()
            .map(Char::toString)

        if (!SendLinkGenerator.isValidSeedCode(seedCode)) {
            return TemporaryAccountState.BrokenLink
        }

        val temporaryAccount = try {
            SendLinkGenerator.parseTemporaryAccount(seedCode)
        } catch (e: Throwable) {
            Timber.e(ReceiveViaLinkError("Failed to parse temporary account", e))
            return TemporaryAccountState.BrokenLink
        }

        val tokenAccounts = rpcAccountRepository.getTokenAccountsByOwner(temporaryAccount.publicKey)
        val activeAccount = tokenAccounts.accounts.firstOrNull {
            it.account.data.parsed.info.tokenAmount.amount.toBigInteger().isMoreThan(BigInteger.ZERO)
        } ?: return checkSolBalance(temporaryAccount)

        val info = activeAccount.account.data.parsed.info
        val tokenData = userLocalRepository.findTokenData(info.mint) ?: run {
            Timber.e(ReceiveViaLinkError("No token data found for mint ${info.mint}"))
            return TemporaryAccountState.ParsingFailed
        }

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

    // if no SPL accounts found, check SOL balance
    private suspend fun checkSolBalance(temporaryAccount: TemporaryAccount): TemporaryAccountState {
        val solBalance = rpcBalanceRepository.getBalance(temporaryAccount.publicKey)
        if (solBalance == 0L) {
            return TemporaryAccountState.EmptyBalance
        }

        val tokenData = userLocalRepository.findTokenData(WRAPPED_SOL_MINT)
            ?: return TemporaryAccountState.ParsingFailed

        val token = Token.createSOL(
            publicKey = temporaryAccount.publicKey.toBase58(),
            tokenData = tokenData,
            amount = solBalance,
            solPrice = null
        )
        return TemporaryAccountState.Active(
            account = temporaryAccount,
            token = token
        )
    }

    private suspend fun fetchPriceForToken(coingeckoId: String): TokenPrice? {
        val price = userLocalRepository.getPriceByTokenId(coingeckoId)
        if (price != null) return price

        return kotlin.runCatching {
            tokenPricesRemoteRepository.getTokenPriceById(
                tokenId = TokenId(coingeckoId),
                targetCurrency = USD_READABLE_SYMBOL
            )
        }
            .onFailure { Timber.i(it) }
            .getOrNull() // can be skipped if there error, that's ok
    }

    suspend fun receiveTransfer(
        temporaryAccount: TemporaryAccount,
        token: Token.Active,
        recipient: PublicKey
    ): String {
        val senderAccount = Account(temporaryAccount.keypair)

        return sendViaLinkInteractor.sendTransaction(
            senderAccount = senderAccount,
            destinationAddress = recipient,
            token = token,
            lamports = token.totalInLamports,
            memo = BuildConfig.svlMemoClaim,
            isSimulation = false,
            shouldCloseAccount = true
        )
    }
}
