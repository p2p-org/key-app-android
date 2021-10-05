package com.p2p.wallet.auth.ui.username

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
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

            enterUserNameButton.setOnClickListener {
                presenter.registerUsername()
            }
        }
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