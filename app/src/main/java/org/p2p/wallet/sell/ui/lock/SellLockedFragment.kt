package org.p2p.wallet.sell.ui.lock

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSellLockBinding
import org.p2p.wallet.utils.viewbinding.viewBinding

class SellLockedFragment :
    BaseMvpFragment<SellLockedContract.View, SellLockedContract.Presenter>(R.layout.fragment_sell_lock),
    SellLockedContract.View {

    companion object {
        fun create() = SellLockedFragment()
    }

    override val presenter: SellLockedContract.Presenter by inject()
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
