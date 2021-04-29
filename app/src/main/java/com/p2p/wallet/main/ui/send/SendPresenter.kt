package com.p2p.wallet.main.ui.send

import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.dashboard.model.local.Token
import com.p2p.wallet.main.interactor.MainInteractor
import com.p2p.wallet.user.interactor.UserInteractor
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.pow
import kotlin.properties.Delegates

class SendPresenter(
    private val mainInteractor: MainInteractor,
    private val userInteractor: UserInteractor
) : BasePresenter<SendContract.View>(), SendContract.Presenter {

    companion object {
        private const val DESTINATION_USD = "USD"
        private const val VALUE_TO_CONVERT = 10.0
    }

    private var token: Token? by Delegates.observable(null) { _, _, newValue ->
        if (newValue != null) view?.showSourceToken(newValue)
    }

    override fun setSourceToken(newToken: Token) {
        token = newToken
    }

    override fun sendToken(targetAddress: String, amount: Double) {
        val token = token ?: return
        launch {
            try {
                val lamports = VALUE_TO_CONVERT.pow(token.decimals)
                val result = mainInteractor.sendToken(targetAddress, lamports.toLong(), token.tokenSymbol)
                view?.showSuccess()
            } catch (e: Throwable) {
                Timber.e(e, "Error sending token")
                view?.showErrorMessage(e)
            }
        }
    }

    override fun loadInitialData() {
        launch {
            view?.showFullScreenLoading(true)
            val tokens = userInteractor.getTokens()
            val source = tokens.firstOrNull() ?: return@launch
            val exchangeRate = userInteractor.getPriceByToken(source.tokenSymbol, DESTINATION_USD)
            token = source.copy(exchangeRate = exchangeRate)
            view?.showFullScreenLoading(false)
        }
    }

    override fun loadTokensForSelection() {
        launch {
            val tokens = userInteractor.getTokens()
            view?.navigateToTokenSelection(tokens)
        }
    }
}