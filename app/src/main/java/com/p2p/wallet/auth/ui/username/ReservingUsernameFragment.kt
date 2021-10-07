package com.p2p.wallet.auth.ui.username

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Toast
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
import com.geetest.sdk.GT3LoadImageView
import com.geetest.sdk.utils.GT3ServiceNode
import com.p2p.wallet.utils.toast
import kotlin.math.abs

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gt3GeeTestUtils = GT3GeetestUtils(requireContext())
        val gt3ConfigBean = GT3ConfigBean()
        gt3ConfigBean.pattern = 2
        gt3ConfigBean.isCanceledOnTouchOutside = false
        gt3ConfigBean.lang = null
        gt3ConfigBean.timeout = 10000
        gt3ConfigBean.webviewTimeout = 10000

        // 设置验证服务集群节点, 默认为中国节点, 使用其他节点需要相应配置, 否则无法使用验证
//        val node: String = preferences.getString("settings_node", "default")
//        when (node) {
//            "na" -> gt3ConfigBean.gt3ServiceNode = GT3ServiceNode.NODE_NORTH_AMERICA
//            "ng" -> gt3ConfigBean.gt3ServiceNode = GT3ServiceNode.NODE_NORTH_GOOGLE
//            "ipv6" -> gt3ConfigBean.gt3ServiceNode = GT3ServiceNode.NODE_IPV6
//            else -> gt3ConfigBean.gt3ServiceNode = GT3ServiceNode.NODE_CHINA
//        }

        gt3ConfigBean.gt3ServiceNode = GT3ServiceNode.NODE_CHINA
        // 设置自定义 LoadingView
        // 设置自定义 LoadingView
        val gt3LoadImageView = GT3LoadImageView(activity)
        gt3LoadImageView.iconRes = R.drawable.ic_appearance
        gt3LoadImageView.loadViewWidth = 48 // 单位dp

        gt3LoadImageView.loadViewHeight = 48 // 单位dp

        gt3ConfigBean.loadImageView = gt3LoadImageView
//        val radius: Int = preferences.getInt("settings_switch_corners_radius", 0)
        val radius: Int = 0
        gt3ConfigBean.corners = abs(radius)
        gt3ConfigBean.dialogOffsetY = radius

        gt3ConfigBean.listener = object : GT3Listener() {

            override fun onReceiveCaptchaCode(p0: Int) {
                toast("onReceiveCaptchaCode", Toast.LENGTH_SHORT)
            }

            override fun onStatistics(p0: String?) {
                toast("onStatistics", Toast.LENGTH_SHORT)
            }

            override fun onClosed(p0: Int) {
                toast("onClosed", Toast.LENGTH_SHORT)
            }

            override fun onSuccess(p0: String?) {
                toast("onSuccess", Toast.LENGTH_SHORT)
            }

            override fun onFailed(p0: GT3ErrorBean?) {
                toast("onFailed", Toast.LENGTH_SHORT)
            }

            override fun onButtonClick() {
                toast("onButtonClick", Toast.LENGTH_SHORT)
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

            usernameEditText.doAfterTextChanged { presenter.checkUsername(it.toString()) }

            enterUserNameButton.isEnabled = true
            enterUserNameButton.setOnClickListener {
//                gt3GeeTestUtils?.startCustomFlow()
//                presenter.registerUsername()
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
}