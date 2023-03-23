package org.p2p.wallet.newsend.ui.relay

import android.content.res.Resources
import org.p2p.core.model.CurrencyMode
import org.p2p.wallet.newsend.statemachine.SendState
import org.p2p.wallet.newsend.ui.NewSendContract

/**
 * support bridge send
 */
class SendCommonUiRelay constructor(
    private val resources: Resources,
) : SendUiRelay {

    private val currencyMode = CurrencyMode.Token("todo", 2)

    override fun handleFeatureState(state: SendState, view: NewSendContract.View?) {
        view ?: return
        when (state) {
            is SendState.Exception -> handleException(state, view)
            is SendState.Loading -> handleLoading(state, view)
            is SendState.Static -> handleStatic(state, view)
        }
    }

    override fun switchCurrencyMode() {
        currencyMode
        TODO("Not yet implemented")
    }

    private fun handleException(state: SendState.Exception, view: NewSendContract.View) {
        handleStatic(state.lastStaticState, view)
    }

    private fun handleLoading(state: SendState.Loading, view: NewSendContract.View) {
        handleStatic(state.lastStaticState, view)
        when (state) {
            is SendState.Loading.Fee -> SendUiRelayUtils.feeLoading(view, resources)
        }
    }

    private fun handleStatic(state: SendState.Static, view: NewSendContract.View) {
        when (state) {
            SendState.Static.Empty -> Unit
            is SendState.Static.ReadyToSend -> TODO()
            is SendState.Static.TokenNotZero -> TODO()
            is SendState.Static.TokenZero -> {
//                view.showTokenSelection()
            }
        }
    }
}
