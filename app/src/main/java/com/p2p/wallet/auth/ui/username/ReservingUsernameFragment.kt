package com.p2p.wallet.auth.ui.username

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.run {
            edgeToEdge {
                toolbar.fit { Edge.TopArc }
                btnEnterUserName.fitMargin { Edge.BottomArc }
            }
            toolbar.setNavigationOnClickListener { popBackStack() }

            // set clickable part of text
            val writeFeedbackText = SpannableString(getString(R.string.auth_skip_this_step))
            val clickableSpan: ClickableSpan = object : ClickableSpan() {
                override fun onClick(textView: View) {
                    navigateToPinCode()
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                }
            }
            // only for en string
            writeFeedbackText.setSpan(clickableSpan, 12, 27, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            tvYouCanSkip.text = writeFeedbackText
            tvYouCanSkip.movementMethod = LinkMovementMethod.getInstance()
            tvYouCanSkip.highlightColor = Color.TRANSPARENT
        }
    }

    override fun navigateToPinCode() {
        replaceFragment(CreatePinFragment.create(PinLaunchMode.CREATE))
    }
}