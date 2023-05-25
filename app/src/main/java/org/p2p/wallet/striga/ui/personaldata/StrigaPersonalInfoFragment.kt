package org.p2p.wallet.striga.ui.personaldata

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentStrigaPersonalInfoBinding
import org.p2p.wallet.utils.viewbinding.viewBinding

class StrigaPersonalInfoFragment :
    BaseMvpFragment<StrigaPersonalInfoContract.View, StrigaPersonalInfoContract.Presenter>(
        R.layout.fragment_striga_personal_info
    ) {

    companion object {
        fun create() = StrigaPersonalInfoFragment()
    }

    override val presenter: StrigaPersonalInfoContract.Presenter by inject()
    private val binding: FragmentStrigaPersonalInfoBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
        }
    }
}
