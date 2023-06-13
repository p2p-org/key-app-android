package org.p2p.wallet.settings.ui.mail

import android.os.Bundle
import android.view.View
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentEmailConfirmBinding
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding

class SettingsEmailConfirmFragment : BaseFragment(R.layout.fragment_email_confirm) {

    companion object {
        fun create(): SettingsEmailConfirmFragment =
            SettingsEmailConfirmFragment()
    }

    private val binding: FragmentEmailConfirmBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setOnMenuItemClickListener {
                return@setOnMenuItemClickListener if (it.itemId == R.id.itemClose) {
                    popBackStack()
                    true
                } else {
                    false
                }
            }

            buttonRestoreGoogle.setOnClickListener {
                // TODO
            }
        }
    }
}
