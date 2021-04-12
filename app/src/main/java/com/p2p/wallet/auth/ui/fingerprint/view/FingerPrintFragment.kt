package com.p2p.wallet.auth.ui.fingerprint.view

import android.os.Bundle
import android.view.View
import com.p2p.wallet.R
import com.p2p.wallet.auth.ui.fingerprint.viewmodel.FingerPrintViewModel
import com.p2p.wallet.common.mvp.BaseFragment
import com.p2p.wallet.databinding.FragmentFingerprintBinding
import com.p2p.wallet.notification.view.NotificationFragment
import com.p2p.wallet.utils.openFingerprintDialog
import com.p2p.wallet.utils.replaceFragment
import com.p2p.wallet.utils.viewbinding.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class FingerPrintFragment : BaseFragment(R.layout.fragment_fingerprint) {

    private val viewModel: FingerPrintViewModel by viewModel()
    private val binding: FragmentFingerprintBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
        }
        binding.btUseFaceID.setOnClickListener {
            requireActivity().openFingerprintDialog {
                viewModel.enableFingerprint()
            }
        }

        with(binding) {
            btLater.setOnClickListener {
                viewModel.doThisLater(false)
                replaceFragment(NotificationFragment.newInstance())
            }
        }
        observeData()
    }

    private fun observeData() {
        viewModel.isSkipFingerPrint.observe(viewLifecycleOwner) {
            viewModel.doThisLater(it)
        }
    }
}