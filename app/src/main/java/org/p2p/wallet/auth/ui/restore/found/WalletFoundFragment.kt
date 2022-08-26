package org.p2p.wallet.auth.ui.restore.found

import androidx.activity.addCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.phone.PhoneNumberEnterFragment
import org.p2p.wallet.auth.web3authsdk.GoogleSignInHelper
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentWalletFoundBinding
import org.p2p.wallet.restore.ui.seedphrase.SeedPhraseFragment
import org.p2p.wallet.intercom.IntercomService
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

    override val statusBarColor: Int = R.color.bg_lime
    override val navBarColor: Int = R.color.bg_night

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
                // TODO make a real restore implementation!
                replaceFragment(SeedPhraseFragment.create())
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

    override fun onSuccessfulSignUp() {
        view?.post {
            replaceFragment(PhoneNumberEnterFragment.create())
        }
    }

    override fun onSameTokenFoundError() {
        replaceFragment(create())
    }

    override fun setLoadingState(isScreenLoading: Boolean) {
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
        showUiKitSnackBar(R.string.error_general_message)
    }

    override fun onConnectionError() {
        setLoadingState(isScreenLoading = false)
        showUiKitSnackBar(getString(R.string.onboarding_offline_error))
    }
}
