package org.p2p.wallet.settings.ui.security

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.BiometricType
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSecurityBinding
import org.p2p.wallet.settings.ui.reset.ResetPinFragment
import org.p2p.wallet.utils.BiometricPromptWrapper
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
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
                if (message == null) popBackStack()
                else AlertDialog.Builder(requireContext())
                    .setMessage(message)
                    .setPositiveButton(R.string.common_ok, null)
                    .show()
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }

            pinCodeView.setOnClickListener {
                replaceFragment(ResetPinFragment.create())
            }
        }

        presenter.loadBiometricType()
    }

    override fun showBiometricData(type: BiometricType) {
        with(binding) {
            when (type) {
                BiometricType.IRIS,
                BiometricType.FACE_ID -> {
                    biometricImageView.setImageResource(R.drawable.ic_faceid)
                    biometricTextView.setText(R.string.settings_security_face_id)
                }
                BiometricType.TOUCH_ID -> {
                    biometricImageView.setImageResource(R.drawable.ic_fingerprint)
                    biometricTextView.setText(R.string.settings_security_fingerprint)
                }
                BiometricType.NONE -> {
                    biometricSwitch.isEnabled = false
                    biometricTextView.setText(R.string.auth_no_biometric_detected)
                }
            }
        }
    }

    override fun showBiometricActive(isActive: Boolean) {
        binding.biometricSwitch.setOnCheckedChangeListener(null)
        binding.biometricSwitch.isChecked = isActive
        binding.biometricSwitch.setOnCheckedChangeListener { _, isChecked ->
            presenter.setBiometricEnabled(isChecked)
        }
    }

    override fun showBiometricEnabled(isEnabled: Boolean) {
        binding.biometricSwitch.isEnabled = isEnabled
    }

    override fun confirmBiometrics(cipher: Cipher) {
        biometricWrapper.authenticate(cipher)
    }
}