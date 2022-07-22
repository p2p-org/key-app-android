package org.p2p.wallet.auth.ui.onboarding

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentContinueOnboardingBinding
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding

class ContinueOnboardingFragment : BaseFragment(R.layout.fragment_continue_onboarding) {

    companion object {
        fun create(): ContinueOnboardingFragment = ContinueOnboardingFragment()
    }

    private val binding: FragmentContinueOnboardingBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            onboardingContinueText.text = getString(R.string.onboarding_continue_subtitle, "test****@gmail.com")
            onboardingContinueButton.setOnClickListener {
                // TODO PWN-4268 make real implementation and move user to phone number screen
                Toast.makeText(context, "In development right now", Toast.LENGTH_SHORT).show()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            popBackStack()
        }
    }
}
