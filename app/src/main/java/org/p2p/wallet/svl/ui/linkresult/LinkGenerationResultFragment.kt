package org.p2p.wallet.svl.ui.linkresult

import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.View
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentSendLinkGenerationResultBinding
import org.p2p.wallet.home.MainFragment
import org.p2p.wallet.newsend.model.LinkGenerationState
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.popBackStackTo
import org.p2p.wallet.utils.shareText
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val EXTRA_STATE = "EXTRA_STATE"

class LinkGenerationResultFragment : BaseFragment(R.layout.fragment_send_link_generation_result) {

    companion object {
        fun create(state: LinkGenerationState): Fragment =
            LinkGenerationResultFragment()
                .withArgs(EXTRA_STATE to state)
    }

    private val state: LinkGenerationState by args(EXTRA_STATE)

    private val binding: FragmentSendLinkGenerationResultBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            renderState()

            imageViewClose.setOnClickListener { popBackStackTo(MainFragment::class) }
        }
    }

    private fun FragmentSendLinkGenerationResultBinding.renderState() {
        when (val state = state) {
            is LinkGenerationState.Success -> {
                imageViewClose.isVisible = true
                viewContent.isVisible = true
                viewError.isVisible = false

                textViewTitle.text = state.amount
                textViewSubtitle.text = state.formattedLink

                buttonAction.setText(R.string.main_share)
                buttonAction.setOnClickListener { shareLink(state.formattedLink) }

                imageViewCopy.setOnClickListener {
                    requireContext().copyToClipBoard(state.formattedLink)
                    showUiKitSnackBar(messageResId = R.string.send_via_link_generation_copied)
                }
            }
            is LinkGenerationState.Error -> {
                viewError.isVisible = true
                viewContent.isVisible = false
                imageViewClose.isVisible = false

                buttonAction.setText(R.string.common_go_back)
                buttonAction.setOnClickListener { popBackStack() }
            }
        }
    }

    private fun shareLink(formattedLink: String) {
        requireContext().shareText(formattedLink)
    }
}
