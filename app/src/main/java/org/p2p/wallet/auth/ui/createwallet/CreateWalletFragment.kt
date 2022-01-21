package org.p2p.wallet.auth.ui.createwallet

import android.os.Bundle
import android.view.View
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.pin.create.CreatePinFragment
import org.p2p.wallet.auth.ui.pin.create.PinLaunchMode
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentCreateWalletBinding
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding

class CreateWalletFragment : BaseFragment(R.layout.fragment_create_wallet) {

    companion object {
        fun create() = CreateWalletFragment()
    }

    private val binding: FragmentCreateWalletBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            progressButton.setOnClickListener {
                replaceFragment(CreatePinFragment.create(PinLaunchMode.CREATE))
            }
        }
    }
}