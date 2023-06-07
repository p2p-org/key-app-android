package org.p2p.wallet.striga.sms

import androidx.activity.addCallback
import androidx.core.os.bundleOf
import android.os.Bundle
import android.view.View
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.core.common.bind
import org.p2p.core.utils.hideKeyboard
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.image.bind
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.common.mvp.NoOpPresenter
import org.p2p.wallet.databinding.FragmentStrigaSmsErrorBinding
import org.p2p.wallet.home.MainFragment
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStackTo
import org.p2p.wallet.utils.viewbinding.getColor
import org.p2p.wallet.utils.viewbinding.viewBinding

class StrigaSmsErrorFragment : BaseMvpFragment<MvpView, NoOpPresenter<MvpView>>(R.layout.fragment_striga_sms_error) {
    companion object {
        private const val ARG_VIEW_TYPE = "ARG_VIEW_TYPE"

        fun create(viewType: StrigaSmsErrorViewType) = StrigaSmsErrorFragment().apply {
            arguments = bundleOf(
                ARG_VIEW_TYPE to viewType
            )
        }
    }

    override val presenter: NoOpPresenter<MvpView> = NoOpPresenter()
    private val binding: FragmentStrigaSmsErrorBinding by viewBinding()
    private val viewType: StrigaSmsErrorViewType by args(ARG_VIEW_TYPE)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.bind(viewType)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            onBackPressed()
        }

        with(binding) {
            buttonOpenWalletScreen.setOnClickListener {
                onBackPressed()
            }
            buttonWriteToSupport.setOnClickListener {
                showHelp()
            }
            toolbar.setNavigationOnClickListener { onBackPressed() }
            toolbar.setOnMenuItemClickListener {
                if (it.itemId == R.id.helpItem) {
                    showHelp()
                    return@setOnMenuItemClickListener true
                }
                false
            }
        }
    }

    private fun FragmentStrigaSmsErrorBinding.bind(viewType: StrigaSmsErrorViewType) {
        imageViewBox.bind(
            ImageViewCellModel(
                DrawableContainer(viewType.imageRes)
            )
        )
        textViewErrorTitle.bind(TextContainer(viewType.titleRes))
        textViewErrorSubtitle.bind(TextContainer(viewType.subtitleRes))
        buttonWriteToSupport.setTextColor(getColor(viewType.helpButtonColorRes))
    }

    private fun onBackPressed() {
        popBackStackTo(MainFragment::class)
    }

    private fun showHelp() {
        view?.hideKeyboard()
        IntercomService.showMessenger()
    }
}
