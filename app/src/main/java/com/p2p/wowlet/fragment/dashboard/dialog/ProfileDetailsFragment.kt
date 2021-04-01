package com.p2p.wowlet.fragment.dashboard.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.p2p.wowlet.R
import com.p2p.wowlet.databinding.FragmentProfileDetailsBinding
import com.p2p.wowlet.utils.popBackStack
import kotlinx.android.synthetic.main.fragment_profile_details.vClose
import kotlinx.android.synthetic.main.fragment_profile_details.vDone
import kotlinx.android.synthetic.main.fragment_profile_details.vLogOut

class ProfileDetailsFragment : Fragment() {

    companion object {

        const val TAG_PROFILE_DETAILS_DIALOG = "ProfileDetailsDialog"
        fun newInstance(): ProfileDetailsFragment {
            return ProfileDetailsFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentProfileDetailsBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_profile_details, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vClose.setOnClickListener {
            popBackStack()
        }
        vDone.setOnClickListener {
            popBackStack()
        }
        vLogOut.setOnClickListener {
            /*viewModel.clearSecretKey()
            viewModel.clearFingerprint()
            activity?.let {
                val intent = Intent(it, RegistrationActivity::class.java)
                it.startActivity(intent)
                it.finish()
            }*/
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