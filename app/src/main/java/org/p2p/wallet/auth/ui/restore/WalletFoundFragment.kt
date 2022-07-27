package org.p2p.wallet.auth.ui.restore

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.common.GoogleSignInHelper
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentWalletFoundBinding
import org.p2p.wallet.restore.ui.keys.SecretKeyFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding

class WalletFoundFragment :
    BaseMvpFragment<WalletFoundContract.View, WalletFoundContract.Presenter>(R.layout.fragment_wallet_found),
    WalletFoundContract.View {

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
                presenter.onSignUpButtonClicked()
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
        // TODO PWN-4268 move user to phone number screen
        setLoadingState(isScreenLoading = false)
        Toast.makeText(requireContext(), "You are successfully signed in!", Toast.LENGTH_SHORT).show()
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
        signInHelper.parseSignInResult(requireContext(), result)?.let { credential ->
            setLoadingState(isScreenLoading = true)
            presenter.setIdToken(credential.id, credential.googleIdToken.orEmpty())
        }
    }
}
