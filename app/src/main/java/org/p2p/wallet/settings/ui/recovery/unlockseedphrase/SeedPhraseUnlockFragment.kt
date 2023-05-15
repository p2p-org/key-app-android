package org.p2p.wallet.settings.ui.recovery.unlockseedphrase

import androidx.fragment.app.setFragmentResultListener
import android.os.Bundle
import android.view.View
import org.p2p.core.utils.insets.appleBottomInsets
import org.p2p.core.utils.insets.appleTopInsets
import org.p2p.core.utils.insets.consume
import org.p2p.core.utils.insets.doOnApplyWindowInsets
import org.p2p.core.utils.insets.systemAndIme
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.pin.validate.ValidatePinFragment
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentSeedPhraseUnlockBinding
import org.p2p.wallet.root.SystemIconsStyle
import org.p2p.wallet.settings.ui.recovery.userseedphrase.UserSeedPhraseFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding

private const val EXTRA_REQUEST_KEY = "EXTRA_REQUEST_PIN_VALIDATE"
private const val EXTRA_RESULT_KEY = "EXTRA_RESULT_PIN_VALIDATE"

class SeedPhraseUnlockFragment : BaseFragment(
    R.layout.fragment_seed_phrase_unlock
) {

    companion object {
        fun create(): SeedPhraseUnlockFragment = SeedPhraseUnlockFragment()
    }

    override val customNavigationBarStyle: SystemIconsStyle = SystemIconsStyle.WHITE
    private val binding: FragmentSeedPhraseUnlockBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            sliderChangePin.onSlideCompleteListener = {
                replaceFragment(ValidatePinFragment.create(EXTRA_REQUEST_KEY, EXTRA_RESULT_KEY))
            }
        }
        setFragmentResultListener(EXTRA_REQUEST_KEY) { key, bundle ->
            val isPinValidated = bundle.getBoolean(EXTRA_RESULT_KEY)
            if (isPinValidated) {
                replaceFragment(UserSeedPhraseFragment.create())
            }
        }
    }

    override fun applyWindowInsets(rootView: View) {
        rootView.doOnApplyWindowInsets { _, insets, _ ->
            insets.systemAndIme().consume {
                binding.toolbar.appleTopInsets(this)
                binding.containerBottomView.appleBottomInsets(this)
            }
        }
    }
}
