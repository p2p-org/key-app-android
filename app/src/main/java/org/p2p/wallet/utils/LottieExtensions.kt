package org.p2p.wallet.utils

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import com.airbnb.lottie.LottieAnimationView

fun LottieAnimationView.doOnAnimationStart(block: (animator: Animator) -> Unit) {
    return addAnimatorListener(object : AnimatorListener {
        override fun onAnimationStart(p0: Animator) {
            block.invoke(p0)
        }

        override fun onAnimationEnd(p0: Animator) = Unit

        override fun onAnimationCancel(p0: Animator) = Unit
        override fun onAnimationRepeat(p0: Animator) = Unit
    })
}

fun LottieAnimationView.doOnAnimationEnd(block: (animator: Animator) -> Unit) {
    return addAnimatorListener(object : AnimatorListener {
        override fun onAnimationStart(p0: Animator) = Unit

        override fun onAnimationEnd(p0: Animator) {
            block.invoke(p0)
        }

        override fun onAnimationCancel(p0: Animator) = Unit
        override fun onAnimationRepeat(p0: Animator) = Unit
    })
}
