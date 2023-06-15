package org.p2p.wallet.settings.ui.mail

import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.animationscreen.AnimationProgressFragment
import org.p2p.wallet.auth.ui.animationscreen.TimerState
import org.p2p.wallet.auth.web3authsdk.GoogleSignInHelper
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentEmailConfirmBinding
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding

class SettingsEmailConfirmFragment :
    BaseMvpFragment<SettingsEmailConfirmContract.View, SettingsEmailConfirmContract.Presenter>(
        R.layout.fragment_email_confirm
    ),
    SettingsEmailConfirmContract.View,
    GoogleSignInHelper.GoogleSignInErrorHandler {

    companion object {
        fun create(): SettingsEmailConfirmFragment =
            SettingsEmailConfirmFragment()
    }

    override val presenter: SettingsEmailConfirmContract.Presenter by inject()
    private val binding: FragmentEmailConfirmBinding by viewBinding()

    private val signInHelper: GoogleSignInHelper by inject()

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
        ::handleSignResult
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setOnMenuItemClickListener {
                return@setOnMenuItemClickListener if (it.itemId == R.id.itemClose) {
                    popBackStack()
                    true
                } else {
                    false
                }
            }

            buttonRestoreGoogle.setOnClickListener {
                startGoogleFlow()
            }
        }
    }

    override fun startGoogleFlow() {
        signInHelper.showSignInDialog(requireContext(), googleSignInLauncher)
    }

    private fun handleSignResult(result: ActivityResult) {
        signInHelper.parseSignInResult(requireContext(), result, errorHandler = this)?.let { credential ->
            setLoadingState(isScreenLoading = true)
            presenter.setGoogleIdToken(credential.id, credential.googleIdToken.orEmpty())
        }
    }

    override fun setLoadingState(isScreenLoading: Boolean) {
        setLoadingAnimationState(isScreenLoading = isScreenLoading)
        with(binding) {
            buttonRestoreGoogle.apply {
                setLoading(isScreenLoading)
                isEnabled = !isScreenLoading
            }
        }
    }

    override fun showSuccessDeviceChange() {
        showUiKitSnackBar(message = getString(R.string.devices_change_success_message))
        popBackStack()
    }

    override fun showFailDeviceChange() {
        showUiKitSnackBar(message = getString(R.string.error_general_message))
        popBackStack()
    }

    override fun onConnectionError() {
        setLoadingState(isScreenLoading = false)
        showUiKitSnackBar(message = getString(R.string.error_general_message))
    }

    override fun onCommonError() {
        setLoadingState(isScreenLoading = false)
        showUiKitSnackBar(messageResId = R.string.onboarding_google_services_error)
    }

    private fun setLoadingAnimationState(isScreenLoading: Boolean) {
        if (isScreenLoading) {
            AnimationProgressFragment.show(
                fragmentManager = requireActivity().supportFragmentManager,
                timerStateList = listOf(
                    TimerState(R.string.devices_change_update_message),
                )
            )
        } else {
            AnimationProgressFragment.dismiss(requireActivity().supportFragmentManager)
        }
    }
}
