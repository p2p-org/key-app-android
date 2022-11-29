package org.p2p.wallet.settings.ui.recovery.user_seed_phrase

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.uikit.organisms.seedphrase.SeedPhraseWord
import org.p2p.uikit.utils.toast
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentUserSeedPhraseBinding
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding

class UserSeedPhraseFragment :
    BaseMvpFragment<UserSeedPhraseContract.View, UserSeedPhraseContract.Presenter>(
        R.layout.fragment_user_seed_phrase
    ),
    UserSeedPhraseContract.View {

    override val presenter: UserSeedPhraseContract.Presenter by inject()
    private val binding: FragmentUserSeedPhraseBinding by viewBinding()
    override val navBarColor: Int = R.color.bg_night

    companion object {
        fun create() = UserSeedPhraseFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener {
                popBackStack()
            }
            with(seedPhraseView) {
                showPasteButton(isVisible = false)
                showClearButton(isVisible = false)
                showBlurButton(isVisible = true)
                setOnBlurStateChangedListener {
                    presenter.onBlurStateChanged(isBlurred = it)
                    binding.imageViewBanner.isSelected = it
                }
                binding.buttonCopy.setOnClickListener {
                    presenter.onCopyClicked()
                }
            }
        }
    }

    override fun showSeedPhase(seedPhaseList: List<SeedPhraseWord>) {
        binding.seedPhraseView.updateSeedPhrase(seedPhaseList)
    }

    override fun copyToClipboard(seedPhase: String) {
        requireContext().copyToClipBoard(seedPhase)
        toast(R.string.common_copied)
    }
}
