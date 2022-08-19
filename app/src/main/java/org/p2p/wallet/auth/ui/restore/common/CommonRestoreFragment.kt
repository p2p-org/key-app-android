package org.p2p.wallet.auth.ui.restore.common

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.phone.PhoneNumberEnterFragment
import org.p2p.wallet.auth.web3authsdk.GoogleSignInHelper
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentCommonRestoreBinding
import org.p2p.wallet.restore.ui.keys.SecretKeyFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding

class CommonRestoreFragment :
    BaseMvpFragment<CommonRestoreContract.View, CommonRestoreContract.Presenter>(
        R.layout.fragment_common_restore
    ),
    CommonRestoreContract.View,
    GoogleSignInHelper.GoogleSignInErrorHandler {

    companion object {
        fun create(): CommonRestoreFragment = CommonRestoreFragment()
    }

    override val presenter: CommonRestoreContract.Presenter by inject { parametersOf(this) }

    private val binding: FragmentCommonRestoreBinding by viewBinding()

    private val signInHelper: GoogleSignInHelper by inject()

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
        ::handleSignResult
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener {
                popBackStack()
            }
            buttonGoogle.setOnClickListener {
                presenter.useGoogleAccount()
            }

            buttonPhone.setOnClickListener {
                replaceFragment(PhoneNumberEnterFragment.create())
            }

            buttonSeed.setOnClickListener {
                // TODO make a real restore implementation!
                replaceFragment(SecretKeyFragment.create())
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            popBackStack()
        }
    }

    override fun startGoogleFlow() {
        signInHelper.showSignInDialog(requireContext(), googleSignInLauncher)
    }

    override fun showError(error: String) {
        view?.post {
            setLoadingState(isScreenLoading = false)
            showErrorSnackBar(error)
        }
    }

    override fun onSuccessfulSignUp() {
        setLoadingState(isScreenLoading = false)
        // TODO check logic here
        replaceFragment(PhoneNumberEnterFragment.create())
    }

    override fun onNoTokenFoundError(userId: String) {
        with(binding) {
            imageView.setImageResource(R.drawable.box_image)
            textViewTitle.text = getString(R.string.restore_no_wallet_title)
            textViewSubtitle.isVisible = true
            textViewSubtitle.text = getString(R.string.restore_no_wallet_subtitle, userId)
        }
    }

    private fun setLoadingState(isScreenLoading: Boolean) {
        with(binding) {
            buttonGoogle.apply {
                isLoading = isScreenLoading
                isEnabled = !isScreenLoading
            }
            buttonPhone.isEnabled = !isScreenLoading
            buttonSeed.isEnabled = !isScreenLoading
        }
    }

    private fun handleSignResult(result: ActivityResult) {
        signInHelper.handler = this
        try {
            signInHelper.parseSignInResult(requireContext(), result)?.let { credential ->
                setLoadingState(isScreenLoading = true)
                presenter.setAlternativeIdToken(credential.id, credential.googleIdToken.orEmpty())
            }
        } finally {
            signInHelper.handler = null
        }
    }

    override fun onConnectionError(error: String) {
        showInfoSnackBar(error)
    }

    override fun onCommonError(error: String) {
        showErrorSnackBar(error)
    }
}
