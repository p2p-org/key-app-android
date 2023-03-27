package org.p2p.wallet.newsend.ui.linkgeneration

import timber.log.Timber
import java.math.BigInteger
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.core.token.Token
import org.p2p.core.utils.fromLamports
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.sendvialink.UserSendLinksLocalRepository
import org.p2p.wallet.infrastructure.sendvialink.model.UserSendLink
import org.p2p.wallet.newsend.interactor.SendInteractor
import org.p2p.wallet.newsend.model.LinkGenerationState
import org.p2p.wallet.newsend.model.TemporaryAccount

private val GENERATION_DELAY = 2.seconds

class SendLinkGenerationPresenter(
    private val sendInteractor: SendInteractor,
    private val userSendLinksRepository: UserSendLinksLocalRepository
) : BasePresenter<SendLinkGenerationContract.View>(),
    SendLinkGenerationContract.Presenter {

    override fun generateLink(recipient: TemporaryAccount, token: Token.Active, lamports: BigInteger) {
        launch {
            val result = try {
                val memo = BuildConfig.sendViaLinkMemo
                delay(GENERATION_DELAY.inWholeMilliseconds)

                sendInteractor.sendTransaction(
                    destinationAddress = recipient.publicKey,
                    token = token,
                    lamports = lamports,
                    memo = memo
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
