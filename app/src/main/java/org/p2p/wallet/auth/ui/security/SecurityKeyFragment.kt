package org.p2p.wallet.auth.ui.security

import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.io.File
import org.p2p.uikit.utils.attachAdapter
import org.p2p.uikit.utils.toast
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.verify.VerifySecurityKeyFragment
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSecurityKeyBinding
import org.p2p.wallet.utils.PixelCopy
import org.p2p.wallet.utils.PixelCopyListener
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.shareScreenShot
import org.p2p.wallet.utils.viewbinding.viewBinding

@Deprecated("Old onboarding flow, delete someday")
class SecurityKeyFragment :
    BaseMvpFragment<SecurityKeyContract.View, SecurityKeyContract.Presenter>(R.layout.fragment_security_key),
    SecurityKeyContract.View,
    PixelCopyListener {

    companion object {
        fun create() = SecurityKeyFragment()
    }

    override val presenter: SecurityKeyContract.Presenter by inject()

    private val binding: FragmentSecurityKeyBinding by viewBinding()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()
    private val keysAdapter: KeysAdapter by lazy {
        KeysAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        analyticsInteractor.logScreenOpenEvent(ScreenNames.OnBoarding.CREATE_MANUAL)
        binding.run {
            toolbar.setNavigationOnClickListener { popBackStack() }
            termsAndConditionsTextView.text = buildTermsAndPrivacyText()
            termsAndConditionsTextView.movementMethod = LinkMovementMethod.getInstance()
            nextButton.setOnClickListener {
                presenter.cacheKeys()
            }
            renewButton.setOnClickListener {
                presenter.loadKeys()
            }
            copyButton.setOnClickListener {
                presenter.copyKeys()
            }
            saveButton.setOnClickListener {
                presenter.saveKeys()
            }

            with(keysRecyclerView) {
                attachAdapter(keysAdapter)
                layoutManager = GridLayoutManager(requireContext(), 3)
            }
        }
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

    override fun showKeys(keys: List<String>) {
        keysAdapter.setItems(keys)
    }

    override fun showLoading(isLoading: Boolean) {
        binding.progressView.isVisible = isLoading
    }

    override fun navigateToVerify(keys: List<String>) {
        replaceFragment(VerifySecurityKeyFragment.create(keys))
    }

    override fun captureKeys() {
        PixelCopy.getBitmapView(binding.root, requireActivity().window, this@SecurityKeyFragment)
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

    override fun shareScreenShot(file: File) {
        requireContext().shareScreenShot(file)
    }

    override fun copyToClipboard(keys: List<String>) {
        if (keys.isNotEmpty()) {
            val data = keys.joinToString(separator = " ")
            requireContext().copyToClipBoard(data)
            toast(R.string.common_copied)
        }
    }

    override fun onCopySuccess(bitmap: Bitmap) {
        presenter.createScreenShootFile(bitmap)
    }

    override fun onCopyError() {
        toast(R.string.error_take_screenshot)
    }
}
