package org.p2p.wallet.auth.ui.restore.found

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.phone.PhoneNumberEnterFragment
import org.p2p.wallet.auth.web3authsdk.GoogleSignInHelper
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentWalletFoundBinding
import org.p2p.wallet.restore.ui.keys.SecretKeyFragment
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

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
        ::handleSignResult
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            walletFoundToolbar.setNavigationOnClickListener {
                popBackStack()
            }
            walletFoundAnotherAccountButton.setOnClickListener {
                presenter.useAnotherGoogleAccount()
            }
            walletFoundRestoreButton.setOnClickListener {
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

    override fun setUserId(userId: String) {
        binding.walletFoundSubtitle.text = getString(R.string.wallet_found_subtitle, userId)
    }

    override fun showError(error: String) {
        view?.post {
            setLoadingState(isScreenLoading = false)
            showErrorSnackBar(error)
        }
    }

    override fun onSuccessfulSignUp() {
        setLoadingState(isScreenLoading = false)
        replaceFragment(PhoneNumberEnterFragment.create())
    }

    private fun setLoadingState(isScreenLoading: Boolean) {
        with(binding) {
            walletFoundAnotherAccountButton.apply {
                isLoading = isScreenLoading
                isEnabled = !isScreenLoading
            }
            walletFoundRestoreButton.isEnabled = !isScreenLoading
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
