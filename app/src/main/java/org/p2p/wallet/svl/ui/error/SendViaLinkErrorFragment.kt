package org.p2p.wallet.svl.ui.error

import androidx.activity.addCallback
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.common.mvp.NoOpPresenter
import org.p2p.wallet.databinding.FragmentSendViaLinkErrorBinding
import org.p2p.wallet.svl.ui.send.SvlReceiveFundsAnalytics
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_ERROR = "ARG_ERROR"

enum class SendViaLinkError {
    ALREADY_CLAIMED, BROKEN_LINK, UNKNOWN
}

class SendViaLinkErrorFragment :
    BaseMvpFragment<MvpView, NoOpPresenter<MvpView>>(R.layout.fragment_send_via_link_error) {

    companion object {
        fun create(error: SendViaLinkError): SendViaLinkErrorFragment =
            SendViaLinkErrorFragment()
                .withArgs(ARG_ERROR to error)
    }

    override val presenter = NoOpPresenter<MvpView>()
    private val receiveFundsAnalytics: SvlReceiveFundsAnalytics by inject()

    private val binding: FragmentSendViaLinkErrorBinding by viewBinding()

    private val error: SendViaLinkError by args(ARG_ERROR)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when (error) {
            SendViaLinkError.ALREADY_CLAIMED -> renderAlreadyClaimed()
            SendViaLinkError.BROKEN_LINK -> renderBrokenLink()
            SendViaLinkError.UNKNOWN -> renderUnknown()
        }
        binding.buttonAction.setOnClickListener {
            popBackStack()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            popBackStack()
        }
    }

    private fun renderAlreadyClaimed() = with(binding) {
        receiveFundsAnalytics.logClaimAlreadyClaimed()

        imageViewBanner.setImageResource(R.drawable.sms_error)
        textViewTitle.setText(R.string.send_via_link_error_already_claimed_title)
        textViewSubtitle.setText(R.string.send_via_link_error_already_claimed_subtitle)
        buttonAction.setText(R.string.send_via_link_error_okay_button)
    }

    private fun renderBrokenLink() = with(binding) {
        imageViewBanner.setImageResource(R.drawable.ic_not_found)
        textViewTitle.setText(R.string.send_via_link_error_broken_link_title)
        textViewSubtitle.setText(R.string.send_via_link_error_broken_link_subtitle)
        buttonAction.setText(R.string.send_via_link_error_okay_button)
    }

    private fun renderUnknown() = with(binding) {
        imageViewBanner.setImageResource(R.drawable.ic_cat)
        textViewTitle.setText(R.string.common_sorry)
        textViewSubtitle.setText(R.string.error_general_message)
        buttonAction.setText(R.string.common_go_back)
    }
}
