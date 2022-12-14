package org.p2p.wallet.moonpay.ui.transaction

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.common.mvp.BaseMvpBottomSheet
import org.p2p.wallet.databinding.DialogSendTransactionDetailsBinding
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.R

class BuyTransactionDetailsBottomSheet : BaseMvpBottomSheet<
    BuyTransactionDetailsContract.View, BuyTransactionDetailsContract.Presenter>(
    R.layout.dialog_send_transaction_details
) {

    override val presenter: BuyTransactionDetailsContract.Presenter by inject()
    private val binding: DialogSendTransactionDetailsBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
