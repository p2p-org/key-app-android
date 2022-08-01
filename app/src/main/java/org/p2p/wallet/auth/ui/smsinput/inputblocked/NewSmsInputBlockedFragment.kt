package org.p2p.wallet.auth.ui.smsinput.inputblocked

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.smsinput.inputblocked.NewAuthSmsInputBlockedContract.Presenter
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentNewSmsInputBlockedBinding
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.auth.ui.smsinput.inputblocked.NewAuthSmsInputBlockedContract.View as ContractView

class NewSmsInputBlockedFragment :
    BaseMvpFragment<ContractView, Presenter>(R.layout.fragment_new_sms_input_blocked),
    ContractView {

    companion object {
        fun create(): NewSmsInputBlockedFragment = NewSmsInputBlockedFragment()
    }

    private val binding: FragmentNewSmsInputBlockedBinding by viewBinding()

    override val presenter: Presenter by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // todo PWN-4362 : add navigation when clicking on starting button and navigation when timer runs out
    }

    override fun renderTimerBeforeUnblockValue(formattedTime: String) {
        binding.textViewSubtitle.text = getString(R.string.onboarding_sms_input_blocked_timer_subtitle, formattedTime)
    }
}
