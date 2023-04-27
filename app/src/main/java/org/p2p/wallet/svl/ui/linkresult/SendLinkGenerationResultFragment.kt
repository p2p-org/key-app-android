package org.p2p.wallet.svl.ui.linkresult

import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentSendLinkGenerationResultBinding
import org.p2p.wallet.home.MainFragment
import org.p2p.wallet.newsend.model.LinkGenerationState
import org.p2p.wallet.svl.analytics.SendViaLinkAnalytics
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.popBackStackTo
import org.p2p.wallet.utils.shareText
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val EXTRA_STATE = "EXTRA_STATE"

class SendLinkGenerationResultFragment : BaseFragment(R.layout.fragment_send_link_generation_result) {

    companion object {
        fun create(state: LinkGenerationState): Fragment =
            SendLinkGenerationResultFragment()
                .withArgs(EXTRA_STATE to state)
    }

    private val state: LinkGenerationState by args(EXTRA_STATE)

    private val binding: FragmentSendLinkGenerationResultBinding by viewBinding()

    private val svlAnalytics: SendViaLinkAnalytics by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            renderState()

            imageViewClose.setOnClickListener { popBackStackTo(MainFragment::class) }
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                popBackStack()
            }
        }
    }

    private fun FragmentSendLinkGenerationResultBinding.renderState() {
        when (val state = state) {
            is LinkGenerationState.Success -> {
                svlAnalytics.logLinkGeneratedSuccessOpened(
                    tokenSymbol = state.tokenSymbol,
                    tokenAmount = state.amount,
                    temporaryAccountPublicKey = state.temporaryAccountPublicKey
                )

                imageViewClose.isVisible = true
                viewContent.isVisible = true
                viewError.isVisible = false

                textViewTitle.text = state.amount
                textViewSubtitle.text = state.formattedLink

                buttonAction.setText(R.string.main_share)
                buttonAction.setOnClickListener {
                    svlAnalytics.logLinkShareButtonClicked()
                    val shareMessage = buildShareLink(state.formattedLink, state.amount)
                    requireContext().shareText(shareMessage)
                }

                imageViewCopy.setOnClickListener {
                    svlAnalytics.logLinkCopyIconClicked()
                    requireContext().copyToClipBoard(state.formattedLink)
                    showUiKitSnackBar(messageResId = R.string.send_via_link_generation_copied)
                }
            }
            is LinkGenerationState.Error -> {
                svlAnalytics.logLinkGeneratedErrorOpened()

                viewError.isVisible = true
                viewContent.isVisible = false
                imageViewClose.isVisible = false

                buttonAction.setText(R.string.common_go_back)
                buttonAction.setOnClickListener { popBackStack() }
            }
        }
    }

    private fun buildShareLink(formattedLink: String, amount: String): String {
        return getString(R.string.send_via_link_share_message, amount, formattedLink)
    }
}
