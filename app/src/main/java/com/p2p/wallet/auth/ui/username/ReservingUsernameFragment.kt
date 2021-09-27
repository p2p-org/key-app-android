package com.p2p.wallet.auth.ui.username

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import com.p2p.wallet.R
import com.p2p.wallet.common.mvp.BaseMvpFragment
import com.p2p.wallet.databinding.FragmentReservingUsernameBinding
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

        // set clickable part of text
        val writeFeedbackText = SpannableString(getString(R.string.auth_skip_this_step))
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }
        // only for en string
        writeFeedbackText.setSpan(clickableSpan, 12, 27, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.tvYouCanSkip.text = writeFeedbackText
    }

    override fun navigateToPinCode() {
        TODO("Not yet implemented")
    }
}