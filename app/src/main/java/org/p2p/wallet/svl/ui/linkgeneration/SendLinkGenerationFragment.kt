package org.p2p.wallet.svl.ui.linkgeneration

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import java.math.BigInteger
import org.p2p.core.token.Token
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSendLinkGenerationBinding
import org.p2p.wallet.newsend.model.LinkGenerationState
import org.p2p.wallet.newsend.model.TemporaryAccount
import org.p2p.wallet.svl.ui.linkresult.LinkGenerationResultFragment
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val EXTRA_TEMPORARY_ACCOUNT = "EXTRA_TEMPORARY_ACCOUNT"
private const val EXTRA_TOKEN = "EXTRA_TOKEN"
private const val EXTRA_LAMPORTS = "EXTRA_LAMPORTS"

class SendLinkGenerationFragment :
    BaseMvpFragment<SendLinkGenerationContract.View, SendLinkGenerationContract.Presenter>(
        R.layout.fragment_send_link_generation
    ),
    SendLinkGenerationContract.View {

    companion object {
        fun create(
            recipient: TemporaryAccount,
            token: Token.Active,
            lamports: BigInteger
        ): Fragment = SendLinkGenerationFragment()
            .withArgs(
                EXTRA_TEMPORARY_ACCOUNT to recipient,
                EXTRA_TOKEN to token,
                EXTRA_LAMPORTS to lamports
            )
    }

    override val presenter: SendLinkGenerationContract.Presenter by inject()

    private val binding: FragmentSendLinkGenerationBinding by viewBinding()

    private val recipient: TemporaryAccount by args(EXTRA_TEMPORARY_ACCOUNT)
    private val token: Token.Active by args(EXTRA_TOKEN)
    private val lamports: BigInteger by args(EXTRA_LAMPORTS)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.generateLink(recipient, token, lamports)
    }

    override fun showResult(state: LinkGenerationState) {
        popAndReplaceFragment(target = LinkGenerationResultFragment.create(state))
    }
}
