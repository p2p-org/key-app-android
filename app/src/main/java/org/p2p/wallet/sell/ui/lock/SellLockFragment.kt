package org.p2p.wallet.sell.ui.lock

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSellLockBinding
import org.p2p.wallet.utils.viewbinding.viewBinding

class SellLockFragment :
    BaseMvpFragment<SellLockContract.View, SellLockContract.Presenter>(R.layout.fragment_sell_lock),
    SellLockContract.View {

    companion object {
        fun create() = SellLockFragment()
    }

    override val presenter: SellLockContract.Presenter by inject()
    private val binding: FragmentSellLockBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            buttonRemove.setOnClickListener {
                presenter.removeFromHistory()
            }
            buttonSend.setOnClickListener {
                presenter.onSendClicked()
            }
            textViewRecipient.setOnClickListener {
                presenter.onRecipientClicked()
            }
            imageViewCopy.setOnClickListener {
                presenter.onCopyClicked()
            }
        }
    }
}
