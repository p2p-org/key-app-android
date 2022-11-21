package org.p2p.wallet.settings.ui.recovery.seed

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentRecoveryKitBinding
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding

class SeedPhraseFragment :
    BaseMvpFragment<SeedPhraseContract.View, SeedPhraseContract.Presenter>(R.layout.fragment_recovery_kit),
    SeedPhraseContract.View {

    companion object {
        fun create(): SeedPhraseFragment = SeedPhraseFragment()
    }

    private val binding: FragmentRecoveryKitBinding by viewBinding()

    override val presenter: SeedPhraseContract.Presenter by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            imageViewHelp.setOnClickListener {
                IntercomService.showMessenger()
            }
        }
    }

    override fun showSeedPhrase(seedPhrase: List<String>) {
        // TODO use seed
    }
}
