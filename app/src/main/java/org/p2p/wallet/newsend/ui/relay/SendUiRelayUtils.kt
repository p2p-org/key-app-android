package org.p2p.wallet.newsend.ui.relay

import android.content.res.Resources
import org.p2p.core.common.TextContainer
import org.p2p.wallet.R
import org.p2p.wallet.newsend.ui.NewSendContract

object SendUiRelayUtils {

    fun feeLoading(view: NewSendContract.View, resources: Resources) {
        view.showFeeViewLoading(true)
        view.setFeeLabel(resources.getString(R.string.send_fees))
        view.setBottomButtonText(TextContainer.Res(R.string.send_calculating_fees))
    }
}
