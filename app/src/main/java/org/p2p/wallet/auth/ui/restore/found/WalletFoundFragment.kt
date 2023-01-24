package org.p2p.wallet.auth.ui.restore.found

import androidx.activity.addCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.core.utils.insets.doOnApplyWindowInsets
import org.p2p.core.utils.insets.systemAndIme
import org.p2p.uikit.natives.UiKitSnackbarStyle
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.animationscreen.AnimationProgressFragment
import org.p2p.wallet.auth.ui.phone.PhoneNumberEnterFragment
import org.p2p.wallet.auth.ui.restore.common.CommonRestoreFragment
import org.p2p.wallet.auth.web3authsdk.GoogleSignInHelper
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentWalletFoundBinding
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.root.SystemIconsStyle
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding

class WalletFoundFragment :
    BaseMvpFragment<WalletFoundContract.View, WalletFoundContract.Presenter>(R.layout.fragment_wallet_found),
    WalletFoundContract.View,
    GoogleSignInHelper.GoogleSignInErrorHandler {

    companion object {
        fun create(): WalletFoundFragment = WalletFoundFragment()
    }

    override val presenter: WalletFoundContract.Presenter by inject()

    private val binding: FragmentWalletFoundBinding by viewBinding()

    private val signInHelper: GoogleSignInHelper by inject()

    override val snackbarStyle: UiKitSnackbarStyle = UiKitSnackbarStyle.WHITE
    override val customStatusBarStyle = SystemIconsStyle.BLACK
    override val customNavigationBarStyle = SystemIconsStyle.WHITE

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
        ::handleSignResult
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            with(toolbarWalletFound) {
                setNavigationOnClickListener {
                    popBackStack()
                }
                setOnMenuItemClickListener {
                    if (it.itemId == R.id.helpItem) {
                        IntercomService.showMessenger()
                        true
                    } else {
                        false
                    }
                }
            }

            buttonUseAnotherAccount.setOnClickListener {
                presenter.useAnotherGoogleAccount()
            }
            buttonStartRestore.setOnClickListener {
                presenter.startRestoreWallet()
                replaceFragment(CommonRestoreFragment.create())
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            popBackStack()
        }
    }

    override fun applyWindowInsets(rootView: View) {
        rootView.doOnApplyWindowInsets { _, insets, _ ->
            val systemAndIme = insets.systemAndIme()
            rootView.updatePadding(top = systemAndIme.top)
            binding.walletFoundBottomContainer.updatePadding(bottom = systemAndIme.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun startGoogleFlow() {
        signInHelper.showSignInDialog(requireContext(), googleSignInLauncher)
    }

    override fun setUserId(userId: String) {
        binding.walletFoundSubtitle.text = getString(R.string.wallet_found_subtitle, userId)
    }

    override fun onSuccessfulSignUp() {
        requireView().post {
            replaceFragment(PhoneNumberEnterFragment.create())
        }
    }

    override fun onSameTokenFoundError() {
        replaceFragment(create())
    }

    override fun setLoadingState(isScreenLoading: Boolean) {
        setLoadingAnimationState(isScreenLoading = isScreenLoading)
        with(binding) {
            buttonUseAnotherAccount.apply {
                isLoadingState = isScreenLoading
                isEnabled = !isScreenLoading
            }
            buttonStartRestore.isEnabled = !isScreenLoading
        }
    }

    private fun handleSignResult(result: ActivityResult) {
        signInHelper.parseSignInResult(requireContext(), result, errorHandler = this)?.let { credential ->
            setLoadingState(isScreenLoading = true)
            presenter.setAlternativeIdToken(credential.id, credential.googleIdToken.orEmpty())
        }
    }

    override fun onCommonError() {
        setLoadingState(isScreenLoading = false)
        showUiKitSnackBar(messageResId = R.string.onboarding_google_services_error)
    }

    override fun onConnectionError() {
        setLoadingState(isScreenLoading = false)
        showUiKitSnackBar(getString(R.string.common_offline_error))
    }

    private fun setLoadingAnimationState(isScreenLoading: Boolean) {
        if (isScreenLoading) {
            AnimationProgressFragment.show(requireActivity().supportFragmentManager, isCreation = false)
        } else {
            AnimationProgressFragment.dismiss(requireActivity().supportFragmentManager)
        }
    }
}
