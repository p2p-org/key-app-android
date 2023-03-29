package org.p2p.wallet.svl.ui.error

import android.os.Bundle
import android.view.View
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.common.mvp.NoOpPresenter
import org.p2p.wallet.databinding.FragmentSendViaLinkErrorBinding
import org.p2p.wallet.home.MainFragment
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStackTo
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_ERROR = "ARG_ERROR"

enum class SendViaLinkError {
    ALREADY_CLAIMED, PARSING_FAILED, BROKEN_LINK, UNKNOWN
}

class SendViaLinkErrorFragment :
    BaseMvpFragment<MvpView, NoOpPresenter<MvpView>>(R.layout.fragment_send_via_link_error) {

    companion object {
        fun create(error: SendViaLinkError): SendViaLinkErrorFragment =
            SendViaLinkErrorFragment()
                .withArgs(ARG_ERROR to error)
    }

    override val presenter = NoOpPresenter<MvpView>()

    private val binding: FragmentSendViaLinkErrorBinding by viewBinding()

    private val error: SendViaLinkError by args(ARG_ERROR)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when (error) {
            SendViaLinkError.ALREADY_CLAIMED -> renderAlreadyClaimed()
            SendViaLinkError.UNKNOWN -> renderUnknown()
        }
        binding.buttonAction.setOnClickListener {
            popBackStackTo(MainFragment::class)
        }
    }

    private fun renderAlreadyClaimed() = with(binding) {
        imageViewBanner.setImageResource(R.drawable.sms_error)
        textViewTitle.setText(R.string.send_via_link_error_already_claimed_title)
        textViewSubtitle.setText(R.string.send_via_link_error_already_claimed_subtitle)
        buttonAction.setText(R.string.send_via_link_error_already_claimed_button)
    }

    private fun renderUnknown() = with(binding) {
        imageViewBanner.setImageResource(R.drawable.ic_cat)
        textViewTitle.setText(R.string.common_sorry)
        textViewSubtitle.setText(R.string.error_general_message)
        buttonAction.setText(R.string.common_go_back)
    }
}
