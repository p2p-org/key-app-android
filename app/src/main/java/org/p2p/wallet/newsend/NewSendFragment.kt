package org.p2p.wallet.newsend

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSendNewBinding
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

class NewSendFragment :
    BaseMvpFragment<NewSendContract.View, NewSendContract.Presenter>(R.layout.fragment_send_new),
    NewSendContract.View {

    companion object {
        private const val ARG_RECIPIENT_ADDRESS = "ARG_RECIPIENT_ADDRESS"
        private const val ARG_RECIPIENT_USERNAME = "ARG_RECIPIENT_USERNAME"

        fun create(recipientAddress: Base58String, recipientUsername: String?) =
            NewSendFragment()
                .withArgs(
                    ARG_RECIPIENT_ADDRESS to recipientAddress.base58Value,
                    ARG_RECIPIENT_USERNAME to recipientUsername
                )
    }

    private val recipientAddress: String by args(ARG_RECIPIENT_ADDRESS)
    private val recipientUsername: String? by args(ARG_RECIPIENT_USERNAME)

    private val binding: FragmentSendNewBinding by viewBinding()

    override val presenter: NewSendContract.Presenter by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.title = recipientUsername ?: recipientAddress
    }
}
