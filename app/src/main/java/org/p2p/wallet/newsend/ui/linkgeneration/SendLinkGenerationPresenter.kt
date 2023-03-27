package org.p2p.wallet.newsend.ui.linkgeneration

import timber.log.Timber
import java.math.BigInteger
import kotlinx.coroutines.launch
import org.p2p.core.token.Token
import org.p2p.core.utils.fromLamports
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.sendvialink.UserSendLinksLocalRepository
import org.p2p.wallet.infrastructure.sendvialink.model.UserSendLink
import org.p2p.wallet.newsend.interactor.SendViaLinkInteractor
import org.p2p.wallet.newsend.model.LinkGenerationState
import org.p2p.wallet.newsend.model.TemporaryAccount

class SendLinkGenerationPresenter(
    private val sendViaLinkInteractor: SendViaLinkInteractor,
    private val userSendLinksRepository: UserSendLinksLocalRepository
) : BasePresenter<SendLinkGenerationContract.View>(),
    SendLinkGenerationContract.Presenter {

    override fun generateLink(recipient: TemporaryAccount, token: Token.Active, lamports: BigInteger) {
        launch {
            val result = try {
                val transactionId = sendViaLinkInteractor.sendTransaction(
                    destinationAddress = recipient.publicKey,
                    token = token,
                    lamports = lamports,
                    memo = BuildConfig.sendViaLinkMemo
                )
                saveLink(recipient, token, lamports)

                val tokenAmount = lamports.fromLamports(token.decimals)
                val formattedAmount = "$tokenAmount ${token.tokenSymbol}"
                LinkGenerationState.Success(recipient.generateFormattedLink(), formattedAmount)
            } catch (e: Throwable) {
                Timber.e(e, "Error generating send link")
                LinkGenerationState.Error
            }
            view?.showResult(result)
        }
    }

    private suspend fun saveLink(link: TemporaryAccount, token: Token.Active, sendAmount: BigInteger) {
        userSendLinksRepository.saveUserLink(
            link = UserSendLink(
                link = link.generateFormattedLink(),
                token = token,
                amount = sendAmount.fromLamports(token.decimals),
                dateCreated = System.currentTimeMillis()
            )
        )
    }
}
