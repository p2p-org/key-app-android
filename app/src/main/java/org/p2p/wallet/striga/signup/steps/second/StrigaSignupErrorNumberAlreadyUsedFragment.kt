package org.p2p.wallet.striga.signup.steps.second

import androidx.activity.addCallback
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import android.os.Bundle
import android.view.View
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.core.common.bind
import org.p2p.core.utils.hideKeyboard
import org.p2p.core.utils.insets.doOnApplyWindowInsets
import org.p2p.core.utils.insets.systemAndIme
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.image.bind
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.common.mvp.NoOpPresenter
import org.p2p.wallet.databinding.FragmentStrigaSmsErrorBinding
import org.p2p.wallet.home.ui.container.MainContainerFragment
import org.p2p.wallet.home.ui.wallet.WalletFragment
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.striga.signup.steps.first.StrigaSignUpFirstStepFragment
import org.p2p.wallet.utils.popBackStackTo
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.getColor
import org.p2p.wallet.utils.viewbinding.viewBinding

class StrigaSignupErrorNumberAlreadyUsedFragment : BaseMvpFragment<MvpView, NoOpPresenter<MvpView>>(
    R.layout.fragment_striga_sms_error
) {

    companion object {
        fun create() = StrigaSignupErrorNumberAlreadyUsedFragment()
    }

    override val presenter: NoOpPresenter<MvpView> = NoOpPresenter()
    private val binding: FragmentStrigaSmsErrorBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.bind()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            onBackPressed()
        }

        with(binding) {
            buttonOpenWalletScreen.setOnClickListener {
                val fragment = StrigaSignUpFirstStepFragment.create(scrollToViewId = R.id.editTextPhoneNumber)
                popBackStackTo(WalletFragment::class)
                replaceFragment(fragment)
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

    override fun applyWindowInsets(rootView: View) {
        rootView.doOnApplyWindowInsets { _, insets, _ ->
            val systemAndIme = insets.systemAndIme()
            rootView.updatePadding(top = systemAndIme.top)
            binding.containerBottomButtons.updatePadding(bottom = systemAndIme.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun FragmentStrigaSmsErrorBinding.bind() {
        imageViewBox.bind(
            ImageViewCellModel(
                DrawableContainer(R.drawable.ic_hand_with_square)
            )
        )
        textViewErrorTitle.bind(TextContainer(R.string.striga_signup_error_number_already_used_title))
        textViewErrorSubtitle.bind(TextContainer(R.string.striga_signup_error_number_already_used_subtitle))
        buttonOpenWalletScreen.bind(TextContainer(R.string.striga_signup_error_number_already_used_button))
        buttonWriteToSupport.setTextColor(getColor(R.color.lime))
    }

    private fun onBackPressed() {
        popBackStackTo(MainContainerFragment::class)
    }

    private fun showHelp() {
        view?.hideKeyboard()
        IntercomService.showMessenger()
    }
}
