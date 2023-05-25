package org.p2p.wallet.striga.ui.finish

import androidx.activity.addCallback
import android.os.Bundle
import android.view.View
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.common.mvp.NoOpPresenter
import org.p2p.wallet.databinding.FragmentStrigaSignupFinishBinding
import org.p2p.wallet.home.MainFragment
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.utils.popBackStackTo
import org.p2p.wallet.utils.viewbinding.viewBinding

class StrigaSignupFinishFragment : BaseMvpFragment<MvpView, NoOpPresenter<MvpView>>(
    R.layout.fragment_striga_signup_finish
) {

    companion object {
        fun create(): StrigaSignupFinishFragment = StrigaSignupFinishFragment()
    }

    override val presenter = NoOpPresenter<MvpView>()

    private val binding: FragmentStrigaSignupFinishBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            returnToMain()
        }
        with(binding.toolbar) {
            setOnMenuItemClickListener {
                if (it.itemId == R.id.helpItem) {
                    IntercomService.showMessenger()
                    true
                } else {
                    false
                }
            }
            setNavigationOnClickListener { returnToMain() }
        }

        binding.buttonContinue.setOnClickListener {
            // for next iteration
            // now return back to main
            returnToMain()
        }
    }

    private fun returnToMain() {
        popBackStackTo(MainFragment::class)
    }
}
