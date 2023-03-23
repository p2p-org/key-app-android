package org.p2p.wallet.newsend.ui.confirmsend

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.p2p.wallet.newsend.statemachine.SendBridgeState
import org.p2p.wallet.newsend.statemachine.SendFeatureAction
import org.p2p.wallet.newsend.statemachine.SendState
import org.p2p.wallet.newsend.statemachine.SendStateMachine
import org.p2p.wallet.newsend.statemachine.commonFee
import org.p2p.wallet.newsend.statemachine.validator.SendBridgeValidator
import org.p2p.wallet.newsend.ui.NewSendContract

class SendBridgeSendAction(
    private val validator: SendBridgeValidator,
    private val stateMachine: SendStateMachine,
) {

    private var featureState: SendBridgeState? = null
    private var catchReadyToSwap: Boolean = false
    private var view: NewSendContract.View? = null

    fun attach(presenterScope: CoroutineScope, view: NewSendContract.View) {
        this.view = view
        stateMachine.observe()
            .filterIsInstance<SendBridgeState>()
            .onEach {
                featureState = it
                if (catchReadyToSwap) {
                    catchReadyToSwap = false
                    if (it is SendState.Static.ReadyToSend) {
                        send()
                    }
                }

            }
            .launchIn(presenterScope)
    }

    fun detach() {
        view = null
    }

    fun send() {
        val view = this.view ?: return
        val state = featureState as? SendState.Static.ReadyToSend ?: return
        val isValid = validator.isFeeValid(state.commonFee)

        if (isValid) {
            sendTransaction(state, view)
        } else {
            stateMachine.newAction(SendFeatureAction.RefreshFee)
            catchReadyToSwap = true
        }
    }

    private fun sendTransaction(state: SendState.Static.ReadyToSend, view: NewSendContract.View) {
        // todo
//        view.showProgressDialog("123")
    }
}
