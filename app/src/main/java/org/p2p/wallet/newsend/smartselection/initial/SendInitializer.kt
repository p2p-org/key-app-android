package org.p2p.wallet.newsend.smartselection.initial

import org.p2p.core.token.Token

class SendInitializer(

) {

    private var initialData: SendInitialData? = null

    var sourceToken: Token.Active? = null

    fun setInitialData(initialData: SendInitialData) {
        this.initialData = initialData
    }

    fun initialize() {
        if (initialToken != null) {
            restoreSelectedToken(view, requireToken())
        } else {
            setupInitialToken(view)
        }
    }
}
