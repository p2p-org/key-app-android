package com.p2p.wallet.dashboard.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.p2p.wallet.R
import com.p2p.wallet.auth.interactor.AuthInteractor
import com.p2p.wallet.auth.ui.onboarding.OnboardingFragment
import com.p2p.wallet.databinding.FragmentProfileDetailsBinding
import com.p2p.wallet.utils.popAndReplaceFragment
import com.p2p.wallet.utils.popBackStack
import com.p2p.wallet.utils.viewbinding.viewBinding
import org.koin.android.ext.android.inject

class ProfileDetailsFragment : Fragment() {

    companion object {

        const val TAG_PROFILE_DETAILS_DIALOG = "ProfileDetailsDialog"
        fun newInstance(): ProfileDetailsFragment {
            return ProfileDetailsFragment()
        }
    }

    private val authInteractor: AuthInteractor by inject()

    private val binding: FragmentProfileDetailsBinding by viewBinding()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_profile_details, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            vClose.setOnClickListener {
                popBackStack()
            }
            vDone.setOnClickListener {
                popBackStack()
            }
            vLogOut.setOnClickListener {
                authInteractor.logout()
                popAndReplaceFragment(OnboardingFragment.create(), inclusive = true)
                /*viewModel.clearSecretKey()
                viewModel.clearFingerprint()
                activity?.let {
                    val intent = Intent(it, RegistrationActivity::class.java)
                    it.startActivity(intent)
                    it.finish()
                }*/
            }
        }
    }

    /*
    *
    * ProfileDetailsDialog.newInstance {
                        viewModel.clearSecretKey()
                        viewModel.clearFingerprint()
                        activity?.let {
                            val intent = Intent(it, RegistrationActivity::class.java)
                            it.startActivity(intent)
                            it.finish()
                        }
                    }.show(
                        childFragmentManager,
                        ProfileDetailsDialog.TAG_PROFILE_DETAILS_DIALOG
                    )*/
}