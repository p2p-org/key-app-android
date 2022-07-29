package org.p2p.wallet.auth.ui.smsinput

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.onboarding.NewOnboardingFragment
import org.p2p.wallet.databinding.FragmentNewSmsInputBlockedBinding
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding

class NewSmsInputBlockedFragment : Fragment(R.layout.fragment_new_sms_input_blocked) {

    companion object {
        fun create() = NewSmsInputBlockedFragment()
    }

    private val binding: FragmentNewSmsInputBlockedBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            replaceFragment(NewOnboardingFragment.create())
        }
    }
}
