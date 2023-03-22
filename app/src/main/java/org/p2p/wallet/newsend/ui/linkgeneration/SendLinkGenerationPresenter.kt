package org.p2p.wallet.newsend.ui.linkgeneration

import timber.log.Timber
import java.math.BigInteger
import kotlinx.coroutines.launch
import org.p2p.core.token.Token
import org.p2p.core.utils.fromLamports
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.newsend.interactor.SendViaLinkInteractor
import org.p2p.wallet.newsend.model.LinkGenerationState
import org.p2p.wallet.newsend.model.TemporaryAccount

class SendLinkGenerationPresenter(
    private val sendViaLinkInteractor: SendViaLinkInteractor
) : BasePresenter<SendLinkGenerationContract.View>(),
    SendLinkGenerationContract.Presenter {

    override fun generateLink(recipient: TemporaryAccount, token: Token.Active, lamports: BigInteger) {
        launch {
            try {
                sendViaLinkInteractor.generateLink(
                    destinationAddress = recipient.publicKey,
                    token = token,
                    lamports = lamports,
                    memo = BuildConfig.sendViaLinkMemo
                )
                val tokenAmount = lamports.fromLamports(token.decimals)
                val formattedAmount = "$tokenAmount ${token.tokenSymbol}"
                val state = LinkGenerationState.Success(recipient.generateFormattedLink(), formattedAmount)
                view?.showResult(state)
            } catch (e: Throwable) {
                Timber.e(e, "Error generating send link")
                view?.showResult(LinkGenerationState.Error)
            }
        }
    }
}
