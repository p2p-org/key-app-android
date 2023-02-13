package org.p2p.core.utils

import androidx.annotation.Px
import androidx.core.view.doOnAttach
import androidx.core.view.updateLayoutParams
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import org.p2p.core.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

fun View.enableDynamicMargin(onKeyboardOpenDeltaMargin: Int) {
    val tagId = R.id.view_keyboard_listener_job_tag_id
    val job = getTag(tagId)
    if (job != null) return

    val keyboardListener = context as? KeyboardListener ?: return
    val initialMargin = getInitialViewMargin()

    fun keyboardVisibleMargin(): Unit = updateLayoutParams {
        val layoutParams = (this as? MarginLayoutParams) ?: return@updateLayoutParams
        val newBottomMargin = initialMargin.bottom - onKeyboardOpenDeltaMargin
        if (newBottomMargin < 0) return@updateLayoutParams
        layoutParams.setMargins(initialMargin.left, initialMargin.top, initialMargin.right, newBottomMargin)
    }

    doOnAttach { view ->
        val keyboardStateJob = keyboardListener
            .keyboardState
            .onEach { isKeyboardVisible ->
                if (isKeyboardVisible) keyboardVisibleMargin() else updateMargin(initialMargin)
            }
            .launchIn(viewScope)
        view.setTag(tagId, keyboardStateJob)
    }
}

fun View.disableDynamicMargin() {
    val job = getTag(R.id.view_keyboard_listener_job_tag_id) as? Job ?: return
    job.cancel()
    updateMargin(getInitialViewMargin())
}

fun View.updateMargin(viewMargin: ViewMargin) {
    updateLayoutParams {
        val layoutParams = (this as? MarginLayoutParams) ?: return@updateLayoutParams
        layoutParams.setMargins(viewMargin.left, viewMargin.top, viewMargin.right, viewMargin.bottom)
    }
}

fun View.getInitialViewMargin(): ViewMargin {
    val tagKey = R.id.initial_view_margin_tag_id
    return getTag(tagKey) as? ViewMargin ?: let {
        val margins = recordInitialMarginForView(this)
        setTag(tagKey, margins)
        margins
    }
}

data class ViewMargin(
    @Px val left: Int,
    @Px val top: Int,
    @Px val right: Int,
    @Px val bottom: Int
)

fun recordInitialMarginForView(view: View): ViewMargin {
    val lp = (view.layoutParams as? MarginLayoutParams) ?: return ViewMargin(0, 0, 0, 0)
    return ViewMargin(
        lp.leftMargin, lp.topMargin, lp.rightMargin, lp.bottomMargin
    )
}
