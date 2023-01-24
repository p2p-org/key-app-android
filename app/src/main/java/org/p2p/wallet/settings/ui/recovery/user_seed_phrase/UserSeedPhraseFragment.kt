package org.p2p.wallet.settings.ui.recovery.user_seed_phrase

import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.core.utils.insets.doOnApplyWindowInsets
import org.p2p.core.utils.insets.systemAndIme
import org.p2p.uikit.organisms.seedphrase.SeedPhraseWord
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
                setOnBlurStateChangedListener { isSelected ->
                    presenter.onBlurStateChanged(isBlurred = !isSelected)
                    imageViewBanner.isSelected = isSelected
                }
                buttonCopy.setOnClickListener {
                    presenter.onCopyClicked()
                }
            }
        }
    }

    override fun applyWindowInsets(rootView: View) {
        rootView.doOnApplyWindowInsets { view, insets, initialPadding ->
            val systemAndIme = insets.systemAndIme()
            binding.toolbar.updatePadding(top = systemAndIme.top)
            binding.containerBottomView.updatePadding(bottom = systemAndIme.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun showSeedPhase(seedPhaseList: List<SeedPhraseWord>, isEditable: Boolean) {
        binding.seedPhraseView.updateSeedPhrase(seedPhaseList, isEditable)
    }

    override fun copyToClipboard(seedPhase: String) {
        requireContext().copyToClipBoard(seedPhase)
        showUiKitSnackBar(messageResId = R.string.common_copied_with_emoji)
    }
}
