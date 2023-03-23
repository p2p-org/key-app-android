package org.p2p.wallet.newsend.ui.linkgeneration

import timber.log.Timber
import java.math.BigInteger
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.core.token.Token
import org.p2p.core.utils.fromLamports
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.infrastructure.sendvialink.UserSendLinksLocalRepository
import org.p2p.wallet.infrastructure.sendvialink.model.UserSendLink
import org.p2p.wallet.newsend.interactor.SendInteractor
import org.p2p.wallet.newsend.model.LinkGenerationState
import org.p2p.wallet.newsend.model.TemporaryAccount
import org.p2p.wallet.utils.toBase58Instance

class SendLinkGenerationPresenter(
    private val sendInteractor: SendInteractor,
    private val tokenKeyProvider: TokenKeyProvider,
    private val userSendLinksRepository: UserSendLinksLocalRepository
) : BasePresenter<SendLinkGenerationContract.View>(),
    SendLinkGenerationContract.Presenter {

    override fun generateLink(recipient: TemporaryAccount, token: Token.Active, lamports: BigInteger) {
        launch {
            val result = try {
                val memo = BuildConfig.sendViaLinkMemo
                delay(2000L)

                sendInteractor.sendTransaction(
                    destinationAddress = recipient.publicKey,
                    token = token,
                    lamports = lamports,
                    memo = memo
                )

                val tokenAmount = lamports.fromLamports(token.decimals)
                val formattedAmount = "$tokenAmount ${token.tokenSymbol}"
                userSendLinksRepository.saveUserLink(
                    currentUserAddress = tokenKeyProvider.publicKey.toBase58Instance(),
                    link = UserSendLink(
                        link = recipient.generateFormattedLink(),
                        token = token,
                        amount = tokenAmount,
                        dateCreated = System.currentTimeMillis()
                    )
                )
                LinkGenerationState.Success(recipient.generateFormattedLink(), formattedAmount)
            } catch (e: Throwable) {
                Timber.e(e, "Error generating send link")
                LinkGenerationState.Error
            }
            view?.showResult(result)
        }
    }
}
