package org.p2p.core.utils

import androidx.core.view.doOnAttach
import android.view.View
import org.p2p.core.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach

fun View.enableOnKeyboardOpenDynamicBottomOffset(onKeyboardOpenDeltaOffset: Float) {
    val tagId = R.id.view_keyboard_listener_job_tag_id
    val job = getTag(tagId)
    if (job is Job) job.cancel()
    val keyboardState = observeKeyboardState() ?: return
    doOnAttach { view ->
        keyboardState
            .onEach { isKeyboardVisible ->
                translationY = if (isKeyboardVisible) onKeyboardOpenDeltaOffset else 0f
            }
            .onCompletion { translationY = 0f }
            .launchIn(viewScope)
            .also { job -> view.setTag(tagId, job) }
    }
}

fun View.disableOnKeyboardOpenDynamicBottomOffset() {
    val job = getTag(R.id.view_keyboard_listener_job_tag_id) as? Job ?: return
    job.cancel()
}
