package org.p2p.wallet.newsend

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.uikit.glide.GlideManager
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSendNewBinding
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.Constants
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_RECIPIENT_ADDRESS = "ARG_RECIPIENT_ADDRESS"
private const val ARG_RECIPIENT_USERNAME = "ARG_RECIPIENT_USERNAME"

class NewSendFragment :
    BaseMvpFragment<NewSendContract.View, NewSendContract.Presenter>(R.layout.fragment_send_new),
    NewSendContract.View {

    companion object {
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
    private val glideManager: GlideManager by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.title = recipientUsername ?: recipientAddress
        // TODO PWN-6090 make logic of setting token!
        binding.widgetSendDetails.apply {
            setSwitchLabel(
                getString(R.string.send_switch_to_token, Constants.SOL_SYMBOL)
            )
        }
        // TODO PWN-6090 make button
        binding.sliderSend.setActionText(R.string.send_enter_amount)
    }

    // TODO PWN-6090 connect this method to token selection
    fun showSourceToken(token: Token.Active) {
        with(binding.widgetSendDetails) {
            glideManager.load(imageViewTokenIcon, token.iconUrl)
            textViewTokenName.text = token.tokenSymbol
            textViewTokenTotal.text = token.getFormattedTotal(includeSymbol = true)
            textViewTokenAmountInUsd.text = token.getFormattedUsdTotal()
        }
    }
}
