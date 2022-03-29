package org.p2p.wallet.restore.ui.keys

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.content.FileProvider
import androidx.core.view.children
import androidx.core.view.isVisible
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import org.koin.android.ext.android.inject
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSecretKeyBinding
import org.p2p.wallet.restore.model.SecretKey
import org.p2p.wallet.restore.ui.derivable.DerivableAccountsFragment
import org.p2p.wallet.restore.ui.keys.adapter.SecretPhraseAdapter
import org.p2p.wallet.settings.ui.reset.seedinfo.SeedInfoFragment
import org.p2p.wallet.utils.attachAdapter
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.focusAndShowKeyboard
import org.p2p.wallet.utils.hideKeyboard
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.toast
import org.p2p.wallet.utils.viewbinding.viewBinding
import timber.log.Timber
import java.io.File

class SecretKeyFragment :
    BaseMvpFragment<SecretKeyContract.View, SecretKeyContract.Presenter>(R.layout.fragment_secret_key),
    SecretKeyContract.View {

    companion object {
        fun create() = SecretKeyFragment()
    }

    override val presenter: SecretKeyContract.Presenter by inject()
    private val binding: FragmentSecretKeyBinding by viewBinding()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()

    private val phraseAdapter: SecretPhraseAdapter by lazy {
        SecretPhraseAdapter {
            presenter.setNewKeys(it)
            clearError()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        analyticsInteractor.logScreenOpenEvent(ScreenNames.OnBoarding.IMPORT_MANUAL)
        with(binding) {
            toolbar.setNavigationOnClickListener {
                it.hideKeyboard()
                popBackStack()
            }

            restoreButton.setOnClickListener {
                presenter.verifySeedPhrase()
            }

            keysRecyclerView.layoutManager = FlexboxLayoutManager(requireContext()).also {
                it.flexDirection = FlexDirection.ROW
                it.justifyContent = JustifyContent.FLEX_START
            }
            keysRecyclerView.attachAdapter(phraseAdapter)

            phraseTextView.isVisible = false
            keysRecyclerView.isVisible = true
            phraseAdapter.addSecretKey(SecretKey())

            questionTextView.setOnClickListener {
                replaceFragment(SeedInfoFragment.create())
            }
            keysRecyclerView.children.find { it.id == R.id.keyEditText }?.focusAndShowKeyboard()
            termsAndConditionsTextView.text = buildTermsAndPrivacyText()
            termsAndConditionsTextView.movementMethod = LinkMovementMethod.getInstance()
        }

        val itemsCount = phraseAdapter.itemCount
        setButtonEnabled(itemsCount != 0)
    }

    override fun showSuccess(secretKeys: List<SecretKey>) {
        replaceFragment(DerivableAccountsFragment.create(secretKeys))
    }

    override fun setButtonEnabled(isEnabled: Boolean) {
        binding.restoreButton.isEnabled = isEnabled
    }

    override fun showError(messageRes: Int) {
        binding.errorTextView.setText(messageRes)
        binding.messageTextView.isVisible = false
    }

    override fun showFile(file: File) {
        val fromFile = FileProvider.getUriForFile(
            requireContext(),
            BuildConfig.APPLICATION_ID + ".provider",
            file
        )

        val target = Intent(Intent.ACTION_VIEW)
        target.setDataAndType(fromFile, "application/pdf")
        target.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            startActivity(target)
        } catch (e: ActivityNotFoundException) {
            Timber.e(e, "Cannot open file")
            toast(R.string.error_opening_file)
        }
    }

    private fun clearError() {
        binding.errorTextView.text = emptyString()
        binding.messageTextView.isVisible = true
    }

    private fun buildTermsAndPrivacyText(): SpannableString {
        val message = getString(R.string.auth_agree_terms_and_privacy)
        val span = SpannableString(message)

        /*
        * Applying clickable span for terms of use
        * */
        val clickableTermsOfUse = object : ClickableSpan() {
            override fun onClick(widget: View) {
                presenter.openTermsOfUse()
                analyticsInteractor.logScreenOpenEvent(ScreenNames.OnBoarding.TERMS_OF_USE)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }
        val termsOfUse = getString(R.string.auth_terms_of_use)
        val termsStart = span.indexOf(termsOfUse)
        val termsEnd = span.indexOf(termsOfUse) + termsOfUse.length
        span.setSpan(clickableTermsOfUse, termsStart, termsEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE)

        val privacyPolicy = getString(R.string.auth_privacy_policy)

        /*
        * Applying clickable span for privacy policy
        * */
        val clickablePrivacy = object : ClickableSpan() {
            override fun onClick(widget: View) {
                presenter.openPrivacyPolicy()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }

        val start = span.indexOf(privacyPolicy)
        val end = span.indexOf(privacyPolicy) + privacyPolicy.length
        span.setSpan(clickablePrivacy, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE)

        return span
    }
}
