package org.p2p.wallet.settings.ui.recovery.unlock_seed_phrase

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.pin.validate.ValidatePinFragment
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSeedPhraseUnlockBinding
import org.p2p.wallet.settings.ui.recovery.user_seed_phrase.UserSeedPhraseFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding

private const val EXTRA_REQUEST_KEY = "EXTRA_REQUEST_PIN_VALIDATE"
private const val EXTRA_RESULT_KEY = "EXTRA_RESULT_PIN_VALIDATE"

class SeedPhraseUnlockFragment :
    BaseMvpFragment<SeedPhraseUnlockContract.View, SeedPhraseUnlockContract.Presenter>(
        R.layout.fragment_seed_phrase_unlock
    ),
    SeedPhraseUnlockContract.View {

    companion object {
        fun create(): SeedPhraseUnlockFragment = SeedPhraseUnlockFragment()
    }

    private val binding: FragmentSeedPhraseUnlockBinding by viewBinding()
    override val navBarColor: Int = R.color.bg_night

    override val presenter: SeedPhraseUnlockContract.Presenter by inject()

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
            } else {
                // TODO
            }
        }
    }
}
