package org.p2p.wallet.auth.ui.verify

import android.os.Bundle
import android.view.View
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseFragment

class VerifySecurityKeyFragment : BaseFragment(R.layout.fragment_verify_security_key) {

    companion object {
        fun create() = VerifySecurityKeyFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}