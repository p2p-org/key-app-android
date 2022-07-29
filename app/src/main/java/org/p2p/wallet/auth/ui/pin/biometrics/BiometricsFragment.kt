package org.p2p.wallet.auth.ui.pin.biometrics

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentBiometricsBinding
import org.p2p.wallet.home.MainFragment
import org.p2p.wallet.utils.BiometricPromptWrapper
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import javax.crypto.Cipher

class BiometricsFragment :
    BaseMvpFragment<BiometricsContract.View, BiometricsContract.Presenter>(R.layout.fragment_biometrics),
    BiometricsContract.View {

    companion object {
        private const val ARG_PIN_CODE = "ARG_PIN_CODE"

        fun create(pinCode: String) = BiometricsFragment().withArgs(
            ARG_PIN_CODE to pinCode
        )
    }

    override val presenter: BiometricsContract.Presenter by inject()

    private val binding: FragmentBiometricsBinding by viewBinding()
    private val pinCode: String by args(ARG_PIN_CODE)

    private val biometricWrapper by lazy {
        BiometricPromptWrapper(
            this,
            onError = { presenter.createPin(pinCode, null) },
            onSuccess = { presenter.createPin(pinCode, it) }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            buttonTouchId.setOnClickListener { presenter.enableBiometric() }
            buttonDontUse.setOnClickListener { presenter.finishAuthorization() }
        }
    }

    override fun showBiometricDialog(cipher: Cipher) {
        biometricWrapper.authenticate(cipher)
    }

    override fun onAuthFinished() {
        popAndReplaceFragment(MainFragment.create(), inclusive = true)
    }
}
