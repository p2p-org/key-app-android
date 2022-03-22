package org.p2p.wallet.settings.ui.security

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSecurityBinding
import org.p2p.wallet.utils.BiometricPromptWrapper
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.koin.android.ext.android.inject
import javax.crypto.Cipher

class SecurityFragment :
    BaseMvpFragment<SecurityContract.View, SecurityContract.Presenter>(R.layout.fragment_security),
    SecurityContract.View {

    companion object {
        fun create() = SecurityFragment()
    }

    override val presenter: SecurityContract.Presenter by inject()

    private val binding: FragmentSecurityBinding by viewBinding()

    private val biometricWrapper by lazy {
        BiometricPromptWrapper(
            this,
            onSuccess = { presenter.onBiometricsConfirmed(it) },
            onError = { message ->
                if (message != null) {
                    AlertDialog.Builder(requireContext())
                        .setMessage(message)
                        .setPositiveButton(R.string.common_ok, null)
                        .show()
                }
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
        }
        presenter.load()
    }

    override fun showBiometricActive(isActive: Boolean) {
        // Enable confirmation switch only if biometrics is active
        binding.confirmationSwitch.isEnabled = isActive

        binding.biometricSwitch.setOnCheckedChangeListener(null)
        binding.biometricSwitch.isChecked = isActive
        binding.biometricSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.biometricSwitch.isChecked = !isChecked
                presenter.setBiometricEnabled(isChecked)
            } else {
                presenter.setBiometricEnabled(isChecked)
            }
        }
        binding.biometricBottomTextView.text =
            getString(if (isActive) R.string.settings_registered else R.string.settings_unregistered)
    }

    override fun showBiometricEnabled(isEnabled: Boolean) {
        binding.biometricSwitch.isEnabled = isEnabled
    }

    override fun confirmBiometrics(cipher: Cipher) {
        biometricWrapper.authenticate(cipher)
    }

    override fun showConfirmationEnabled(isEnabled: Boolean) {
        binding.confirmationSwitch.isEnabled = isEnabled
    }

    override fun showConfirmationActive(isActive: Boolean) {
        with(binding) {
            confirmationSwitch.setOnCheckedChangeListener(null)
            confirmationSwitch.isChecked = isActive
            confirmationSwitch.setOnCheckedChangeListener { _, isChecked ->
                presenter.onConfirmationStateChanged(isChecked)
            }
        }
    }
}
