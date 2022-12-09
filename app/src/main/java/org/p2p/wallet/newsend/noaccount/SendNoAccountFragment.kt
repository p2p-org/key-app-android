package org.p2p.wallet.newsend.noaccount

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentSendNoAccountBinding
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_TOKEN_SYMBOL = "ARG_TOKEN_SYMBOL"
private const val ARG_CRITICAL_ISSUE = "ARG_CRITICAL_ISSUE"

class SendNoAccountFragment : BaseFragment(R.layout.fragment_send_no_account) {

    companion object {
        fun create(
            tokenSymbol: String,
            criticalIssue: Boolean
        ): SendNoAccountFragment = SendNoAccountFragment()
            .withArgs(
                ARG_TOKEN_SYMBOL to tokenSymbol,
                ARG_CRITICAL_ISSUE to criticalIssue
            )
    }

    private val tokenSymbol: String by args(ARG_TOKEN_SYMBOL)
    private val criticalIssue: Boolean by args(ARG_CRITICAL_ISSUE)

    override val navBarColor: Int
        get() = R.color.night

    private val binding: FragmentSendNoAccountBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            buttonOk.setOnClickListener { popBackStack() }
            buttonContinue.apply {
                text = getString(R.string.send_no_account_non_critical_continue, tokenSymbol)
                setOnClickListener { popBackStack() }
            }
            textViewTitle.text = getString(R.string.send_no_account_title, tokenSymbol)
            containerBottom.isVisible = criticalIssue
            buttonOk.isVisible = !criticalIssue
            textViewMessage.text = getString(
                if (criticalIssue) {
                    R.string.send_no_account_critical_message
                } else {
                    R.string.send_no_account_non_critical_message
                },
                tokenSymbol
            )
        }
    }
}
