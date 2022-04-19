package org.p2p.wallet.auth.ui.username

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.view.isVisible
import com.geetest.sdk.GT3ConfigBean
import com.geetest.sdk.GT3ErrorBean
import com.geetest.sdk.GT3GeetestUtils
import com.geetest.sdk.GT3Listener
import org.json.JSONObject
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.ReserveMode
import org.p2p.wallet.auth.ui.pin.create.CreatePinFragment
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.widget.InputTextView
import org.p2p.wallet.databinding.FragmentReserveUsernameBinding
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

class ReserveUsernameFragment :
    BaseMvpFragment<ReserveUsernameContract.View,
        ReserveUsernameContract.Presenter>(R.layout.fragment_reserve_username),
    ReserveUsernameContract.View {

    companion object {
        private const val EXTRA_MODE: String = "EXTRA_MODE"
        private const val EXTRA_SKIP_STEP_ENABLED = "EXTRA_SKIP_STEP_ENABLED"
        fun create(
            mode: ReserveMode,
            isSkipStepEnabled: Boolean = true
        ) = ReserveUsernameFragment().withArgs(
            EXTRA_MODE to mode,
            EXTRA_SKIP_STEP_ENABLED to isSkipStepEnabled
        )
    }

    override val presenter: ReserveUsernameContract.Presenter by inject()
    private val binding: FragmentReserveUsernameBinding by viewBinding()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()
    private var gt3GeeTestUtils: GT3GeetestUtils? = null
    private var gt3ConfigBean: GT3ConfigBean? = null

    private val mode: ReserveMode by args(EXTRA_MODE)
    private val isSkipStepEnabled: Boolean by args(EXTRA_SKIP_STEP_ENABLED)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        analyticsInteractor.logScreenOpenEvent(ScreenNames.OnBoarding.USERNAME_RESERVE)
        initGeetestUtils()

        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }

            skipTextView.text = buildClickableText()
            skipTextView.movementMethod = LinkMovementMethod.getInstance()
            skipTextView.highlightColor = Color.TRANSPARENT
            skipTextView.isVisible = isSkipStepEnabled
            inputTextView.doAfterTextChanged {
                presenter.checkUsername(it.toString().lowercase())
            }

            inputTextView.focusAndShowKeyboard()

            usernameButton.setOnClickListener {
                presenter.save()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        gt3GeeTestUtils?.destory()
    }

    override fun navigateToPinCode() {
        replaceFragment(CreatePinFragment.create())
    }

    override fun navigateToUsername() {
        popAndReplaceFragment(UsernameFragment.create())
    }

    override fun showIdleState() {
        binding.inputTextView.setMessageWithState(getString(R.string.auth_use_any_latin), InputTextView.State.Idle)
    }

    override fun showUnavailableName(name: String) {
        with(binding) {
            usernameButton.isEnabled = false
            val message = getString(R.string.auth_unavailable_name, name)
            inputTextView.setMessageWithState(message, InputTextView.State.Error)
        }
    }

    override fun showAvailableName(name: String) {
        with(binding) {
            usernameButton.isEnabled = true
            val message = getString(R.string.auth_available_name, name)
            inputTextView.setMessageWithState(message, InputTextView.State.Success)
        }
    }

    override fun showCaptcha(params: JSONObject) {
        gt3ConfigBean?.api1Json = params
        gt3GeeTestUtils?.getGeetest()
    }

    override fun showCaptchaSucceeded() {
        gt3GeeTestUtils?.showSuccessDialog()
    }

    override fun showCaptchaFailed() {
        gt3GeeTestUtils?.showFailedDialog()
    }

    override fun showSuccess() {
        finishNavigation()
    }

    override fun showLoading(isLoading: Boolean) {
        binding.usernameButton.setLoading(isLoading)
    }

    override fun showUsernameLoading(isLoading: Boolean) {
        val message = getString(R.string.auth_username_searching)
        binding.inputTextView.setMessageWithState(message, InputTextView.State.Loading)
    }

    override fun showCustomFlow() {
        gt3GeeTestUtils?.startCustomFlow()
    }

    private fun initGeetestUtils() {
        gt3GeeTestUtils = GT3GeetestUtils(requireContext())
        gt3ConfigBean = GT3ConfigBean()
        gt3ConfigBean?.pattern = 1
        gt3ConfigBean?.isCanceledOnTouchOutside = false
        gt3ConfigBean?.lang = null
        gt3ConfigBean?.timeout = 10000
        gt3ConfigBean?.webviewTimeout = 10000
        gt3ConfigBean?.listener = object : GT3Listener() {
            override fun onDialogResult(result: String) {
                val username = binding.inputTextView.getText().lowercase()
                presenter.registerUsername(username, result)
                gt3GeeTestUtils?.showSuccessDialog()
            }

            override fun onReceiveCaptchaCode(p0: Int) {
            }

            override fun onStatistics(p0: String?) {
            }

            override fun onClosed(p0: Int) {
            }

            override fun onSuccess(p0: String?) {
            }

            override fun onFailed(p0: GT3ErrorBean?) {
            }

            override fun onButtonClick() {
                presenter.checkCaptcha()
            }
        }
        gt3GeeTestUtils?.init(gt3ConfigBean)
    }

    private fun buildClickableText(): SpannableString {
        val clickableText = getString(R.string.auth_clickable_skip_this_step)
        val message = getString(R.string.auth_skip_this_step)
        val span = SpannableString(message)
        val clickableNumber = object : ClickableSpan() {
            override fun onClick(widget: View) {
                presenter.onSkipClicked()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }
        val start = span.indexOf(clickableText)
        val end = span.indexOf(clickableText) + clickableText.length
        span.setSpan(clickableNumber, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        return span
    }

    override fun finishNavigation() {
        when (mode) {
            ReserveMode.PIN_CODE -> navigateToPinCode()
            ReserveMode.POP -> navigateToUsername()
        }
    }
}
