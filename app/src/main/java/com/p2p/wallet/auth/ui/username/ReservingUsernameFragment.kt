package com.p2p.wallet.auth.ui.username

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.geetest.sdk.GT3GeetestUtils
import com.p2p.wallet.R
import com.p2p.wallet.auth.ui.pin.create.CreatePinFragment
import com.p2p.wallet.auth.ui.pin.create.PinLaunchMode
import com.p2p.wallet.common.mvp.BaseMvpFragment
import com.p2p.wallet.databinding.FragmentReservingUsernameBinding
import com.p2p.wallet.utils.edgetoedge.Edge
import com.p2p.wallet.utils.edgetoedge.edgeToEdge
import com.p2p.wallet.utils.popBackStack
import com.p2p.wallet.utils.replaceFragment
import com.p2p.wallet.utils.viewbinding.viewBinding
import org.koin.android.ext.android.inject
import com.geetest.sdk.GT3Listener

import com.geetest.sdk.GT3ConfigBean
import com.geetest.sdk.GT3ErrorBean
import com.p2p.wallet.utils.colorFromTheme
import org.json.JSONObject

class ReservingUsernameFragment :
    BaseMvpFragment<ReservingUsernameContract.View,
        ReservingUsernameContract.Presenter>(R.layout.fragment_reserving_username),
    ReservingUsernameContract.View {

    companion object {
        fun create() = ReservingUsernameFragment()
    }

    override val presenter: ReservingUsernameContract.Presenter by inject()

    private val binding: FragmentReservingUsernameBinding by viewBinding()
    private var gt3GeeTestUtils: GT3GeetestUtils? = null
    private var gt3ConfigBean: GT3ConfigBean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gt3GeeTestUtils = GT3GeetestUtils(requireContext())
        gt3ConfigBean = GT3ConfigBean()
        gt3ConfigBean?.pattern = 1
        gt3ConfigBean?.isCanceledOnTouchOutside = false
        gt3ConfigBean?.lang = null
        gt3ConfigBean?.timeout = 10000
        gt3ConfigBean?.webviewTimeout = 10000
        gt3ConfigBean?.listener = object : GT3Listener() {
            override fun onDialogResult(result: String?) {
                presenter.registerUsername(binding.usernameEditText.text.toString().lowercase(), result)
                gt3GeeTestUtils?.showSuccessDialog()
            }

            override fun onReceiveCaptchaCode(p0: Int) {
            }

            override fun onStatistics(p0: String?) {
            }

            override fun onClosed(p0: Int) {
            }

            override fun onSuccess(p0: String?) {
                binding.progressView.visibility = View.VISIBLE
            }

            override fun onFailed(p0: GT3ErrorBean?) {
            }

            override fun onButtonClick() {
                presenter.checkCaptcha()
            }
        }
        gt3GeeTestUtils?.init(gt3ConfigBean)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            edgeToEdge {
                toolbar.fit { Edge.TopArc }
                enterUserNameButton.fitMargin { Edge.BottomArc }
            }
            toolbar.setNavigationOnClickListener { popBackStack() }

            youCanSkipTextView.text = buildClickableText()
            youCanSkipTextView.movementMethod = LinkMovementMethod.getInstance()
            youCanSkipTextView.highlightColor = Color.TRANSPARENT

            usernameEditText.doAfterTextChanged {
                presenter.checkUsername(it.toString().lowercase())
            }

            enterUserNameButton.setOnClickListener {
                gt3GeeTestUtils?.startCustomFlow()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        gt3GeeTestUtils?.destory()
    }

    override fun navigateToPinCode() {
        replaceFragment(CreatePinFragment.create(PinLaunchMode.CREATE))
    }

    override fun showUnavailableName(name: String) {
        binding.usernameTextView.text = getString(R.string.auth_unavailable_name, name)
        binding.usernameTextView.setTextColor(colorFromTheme(R.attr.colorAccentWarning))
        binding.enterUserNameButton.isEnabled = false
        setTextColorGrey()
    }

    override fun showAvailableName(name: String) {
        binding.usernameTextView.text = getString(R.string.auth_available_name, name)
        binding.usernameTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorGreen))
        binding.enterUserNameButton.isEnabled = true
        setTextColorGrey()
    }

    override fun getCaptchaResult(params: JSONObject) {
        gt3ConfigBean?.api1Json = params
        gt3GeeTestUtils?.getGeetest()
    }

    override fun successCaptcha() {
        gt3GeeTestUtils?.showSuccessDialog()
    }

    override fun failCaptcha() {
        gt3GeeTestUtils?.showFailedDialog()
    }

    override fun successRegisterName() {
        binding.progressView.visibility = View.GONE
        navigateToPinCode()
    }

    override fun failRegisterName() {
        binding.progressView.visibility = View.GONE
    }

    private fun buildClickableText(): SpannableString {
        val clickableText = getString(R.string.auth_clickable_skip_this_step)
        val message = getString(R.string.auth_skip_this_step)
        val span = SpannableString(message)
        val clickableNumber = object : ClickableSpan() {
            override fun onClick(widget: View) {
                navigateToPinCode()
            }
        }
        val start = span.indexOf(clickableText)
        val end = span.indexOf(clickableText) + clickableText.length
        span.setSpan(clickableNumber, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        return span
    }

    private fun setTextColorGrey() {
        if (binding.usernameEditText.text?.isEmpty() == true) {
            binding.usernameTextView.text = getString(R.string.auth_use_any_latin)
            binding.usernameTextView.setTextColor(colorFromTheme(R.attr.colorElementSecondary))
        }
    }
}