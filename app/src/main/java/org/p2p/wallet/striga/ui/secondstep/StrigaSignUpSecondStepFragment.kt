package org.p2p.wallet.striga.ui.secondstep

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentStrigaSignUpSecondStepBinding
import org.p2p.wallet.utils.viewbinding.viewBinding

typealias IV = StrigaSignUpSecondStepContract.View
typealias IP = StrigaSignUpSecondStepContract.Presenter

class StrigaSignUpSecondStepFragment : BaseMvpFragment<IV, IP>(R.layout.fragment_striga_sign_up_second_step), IV {

    companion object {
        fun create() = StrigaSignUpSecondStepFragment()
    }

    override val presenter: IP by inject()
    private val binding: FragmentStrigaSignUpSecondStepBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
