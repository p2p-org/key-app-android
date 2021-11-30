package org.p2p.wallet.auth.ui.biometric

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import org.p2p.wallet.utils.edgetoedge.edgeToEdge
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.BiometricType
import org.p2p.wallet.auth.ui.done.AuthDoneFragment
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentBiometricBinding
import org.p2p.wallet.utils.BiometricPromptWrapper
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.edgetoedge.Edge
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import org.koin.android.ext.android.inject
import javax.crypto.Cipher

class BiometricFragment :
    BaseMvpFragment<BiometricContract.View, BiometricContract.Presenter>(R.layout.fragment_biometric),
    BiometricContract.View {

    companion object {
        private const val EXTRA_PIN_CODE = "EXTRA_PIN_CODE"
        fun create(createdPin: String) = BiometricFragment().withArgs(
            EXTRA_PIN_CODE to createdPin
        )
    }

    override val presenter: BiometricContract.Presenter by inject()

    private val binding: FragmentBiometricBinding by viewBinding()

    private val pinCode: String by args(EXTRA_PIN_CODE)

    private val biometricWrapper by lazy {
        BiometricPromptWrapper(
            this,
            onError = { popAndReplaceFragment(AuthDoneFragment.create(), inclusive = true) },
            onSuccess = { presenter.createPin(pinCode, it) }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            edgeToEdge {
                toolbar.fit { Edge.TopArc }
                laterButton.fitMargin { Edge.BottomArc }
            }

            enableButton.setOnClickListener {
                presenter.enableBiometric()
            }

            laterButton.setOnClickListener {
                presenter.createPin(pinCode, null)
            }

            when (getBiometricType(requireContext())) {
                BiometricType.IRIS,
                BiometricType.FACE_ID -> {
                    biometricImageView.setImageResource(R.drawable.ic_faceid)
                    biometricTextView.setText(R.string.auth_enable_faceid_question)
                    enableButton.setText(R.string.auth_enable_faceid)
                }
                BiometricType.TOUCH_ID -> {
                    biometricImageView.setImageResource(R.drawable.ic_fingerprint)
                    biometricTextView.setText(R.string.auth_enable_touchid_question)
                    enableButton.setText(R.string.auth_enable_touchid)
                }
                BiometricType.NONE -> {
                    biometricTextView.setText(R.string.auth_no_biometric_detected)
                }
            }
        }
    }

    override fun showBiometricDialog(cipher: Cipher) {
        biometricWrapper.authenticate(cipher)
    }

    override fun onAuthFinished() {
        popAndReplaceFragment(AuthDoneFragment.create(), inclusive = true)
    }

    private fun getBiometricType(context: Context): BiometricType {
        val packageManager: PackageManager = context.packageManager

        // SDK 29 adds FACE and IRIS authentication
        if (Build.VERSION.SDK_INT >= 29) {
            if (packageManager.hasSystemFeature(PackageManager.FEATURE_FACE)) {
                return BiometricType.FACE_ID
            }
            if (packageManager.hasSystemFeature(PackageManager.FEATURE_IRIS)) {
                return BiometricType.IRIS
            }
            return if (packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
                BiometricType.TOUCH_ID
            } else {
                BiometricType.NONE
            }
        }

        // SDK 23-28 offer FINGERPRINT only
        return if (packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
            BiometricType.TOUCH_ID
        } else {
            BiometricType.NONE
        }
    }
}