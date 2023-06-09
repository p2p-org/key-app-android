package org.p2p.wallet.utils

import androidx.core.content.ContextCompat
import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import org.p2p.wallet.R

object OnboardingSpanUtils {

    fun buildTermsAndPolicyText(
        context: Context,
        onTermsClick: () -> Unit,
        onPolicyClick: () -> Unit
    ): SpannableString {
        val message = context.getString(R.string.onboarding_terms_and_policy)
        val span = SpannableString(message)

        /*
        * Applying clickable span for terms of use
        * */
        val clickableTermsOfUse = object : ClickableSpan() {
            override fun onClick(widget: View) {
                onTermsClick()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.apply {
                    color = ContextCompat.getColor(context, R.color.text_rain)
                    isUnderlineText = false
                }
            }
        }
        val termsOfUse = context.getString(R.string.onboarding_terms_of_use)
        val termsStart = span.indexOf(termsOfUse)
        val termsEnd = span.indexOf(termsOfUse) + termsOfUse.length
        span.setSpan(clickableTermsOfUse, termsStart, termsEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE)

        val privacyPolicy = context.getString(R.string.onboarding_privacy_policy)

        /*
        * Applying clickable span for privacy policy
        * */
        val clickablePolicy = object : ClickableSpan() {
            override fun onClick(widget: View) {
                onPolicyClick()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.apply {
                    color = ContextCompat.getColor(context, R.color.text_rain)
                    isUnderlineText = false
                }
            }
        }

        val start = span.indexOf(privacyPolicy)
        val end = span.indexOf(privacyPolicy) + privacyPolicy.length
        span.setSpan(clickablePolicy, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE)

        return span
    }
}
