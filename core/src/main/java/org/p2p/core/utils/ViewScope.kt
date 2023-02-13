package org.p2p.core.utils

import android.view.View
import org.p2p.core.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

val View.viewScope: CoroutineScope
    get() {
        val storedScope = getTag(R.id.view_coroutine_scope_tag_id) as? CoroutineScope
        if (storedScope != null) return storedScope

        val newScope = ViewCoroutineScope()
        if (isAttachedToWindow) {
            addOnAttachStateChangeListener(newScope)
            setTag(R.id.view_coroutine_scope_tag_id, newScope)
        } else newScope.cancel()

        return newScope
    }

private class ViewCoroutineScope : CoroutineScope, View.OnAttachStateChangeListener {
    override val coroutineContext = SupervisorJob() + Dispatchers.Main

    override fun onViewAttachedToWindow(view: View) = Unit

    override fun onViewDetachedFromWindow(view: View) {
        coroutineContext.cancel()
        view.setTag(R.id.view_coroutine_scope_tag_id, null)
    }
}
