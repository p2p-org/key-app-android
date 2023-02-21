package org.p2p.wallet.settings.ui.reset.seedphrase

import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import org.koin.android.ext.android.inject
import org.p2p.core.utils.hideKeyboard
import org.p2p.uikit.organisms.seedphrase.SeedPhraseWord
import org.p2p.uikit.organisms.seedphrase.adapter.SeedPhraseAdapter
import org.p2p.uikit.utils.attachAdapter
import org.p2p.wallet.R
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentResetSeedPhraseBinding
import org.p2p.wallet.settings.ui.reset.seedinfo.SeedInfoFragment
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val EXTRA_REQUEST_KEY = "EXTRA_REQUEST_KEY"
private const val EXTRA_RESULT_KEY = "EXTRA_RESULT_KEY"

@Deprecated("Old onboarding flow, delete someday")
class ResetSeedPhraseFragment :
    BaseMvpFragment<ResetSeedPhraseContract.View, ResetSeedPhraseContract.Presenter>(
        R.layout.fragment_reset_seed_phrase
    ),
    ResetSeedPhraseContract.View {

    override val presenter: ResetSeedPhraseContract.Presenter by inject()
    private val binding: FragmentResetSeedPhraseBinding by viewBinding()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()
    private val requestKey: String by args(EXTRA_REQUEST_KEY)
    private val resultKey: String by args(EXTRA_RESULT_KEY)

    companion object {
        fun create(requestKey: String, resultKey: String) = ResetSeedPhraseFragment()
            .withArgs(
                EXTRA_REQUEST_KEY to requestKey,
                EXTRA_RESULT_KEY to resultKey
            )
    }

    private val phraseAdapter: SeedPhraseAdapter by lazy {
        SeedPhraseAdapter(
            onSeedPhraseChanged = {
                presenter.setNewKeys(it)
                clearError()
            },
            onShowKeyboardListener = {
                // TODO: to be done after this screen is redesigned
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        analyticsInteractor.logScreenOpenEvent(ScreenNames.Settings.PIN_RESET)
        with(binding) {
            toolbar.setNavigationOnClickListener {
                popBackStack()
                it.hideKeyboard()
            }

            restoreButton.setOnClickListener {
                presenter.verifySeedPhrase()
            }

            keysRecyclerView.layoutManager = FlexboxLayoutManager(requireContext()).also {
                it.flexDirection = FlexDirection.ROW
                it.justifyContent = JustifyContent.FLEX_START
            }
            keysRecyclerView.attachAdapter(phraseAdapter)

            phraseTextView.setOnClickListener {
                phraseTextView.isVisible = false
                keysRecyclerView.isVisible = true
                phraseAdapter.addWord(SeedPhraseWord.EMPTY_WORD)
            }

            messageTextView.text = buildSeedInfoText()
            messageTextView.movementMethod = LinkMovementMethod.getInstance()
        }

        val itemsCount = phraseAdapter.itemCount
        setButtonEnabled(itemsCount != 0)
    }

    override fun showSuccess(seedPhrase: List<SeedPhraseWord>) {
        setFragmentResult(requestKey, bundleOf(Pair(resultKey, seedPhrase)))
        popBackStack()
    }

    override fun setButtonEnabled(isEnabled: Boolean) {
        binding.restoreButton.isEnabled = isEnabled
    }

    override fun showError(messageRes: Int) {
        binding.errorTextView.setText(messageRes)
        binding.messageTextView.isVisible = false
    }

    private fun clearError() {
        binding.errorTextView.text = emptyString()
        binding.messageTextView.isVisible = true
    }

    private fun buildSeedInfoText(): SpannableString {
        val seedInfo = getString(R.string.settings_what_is_a_security_key)

        val message = buildString {
            append(getString(R.string.auth_recover_info))
            append("\n\n")
            append(seedInfo)
        }

        val span = SpannableString(message)
        val clickableSeedInfo = object : ClickableSpan() {
            override fun onClick(widget: View) {
                replaceFragment(SeedInfoFragment.create())
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }
        val termsStart = span.indexOf(seedInfo)
        val termsEnd = span.indexOf(seedInfo) + seedInfo.length
        span.setSpan(clickableSeedInfo, termsStart, termsEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE)

        return span
    }
}
