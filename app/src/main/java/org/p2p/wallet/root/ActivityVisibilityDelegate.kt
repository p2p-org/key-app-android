package org.p2p.wallet.root

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface AppActivityVisibility {
    val visibilityState: StateFlow<ActivityVisibility>
}

class ActivityVisibilityDelegate constructor(
    private val activity: AppCompatActivity
) : DefaultLifecycleObserver {

    private val state = MutableStateFlow<ActivityVisibility>(ActivityVisibility.Initializing)

    init {
        activity.lifecycle.addObserver(this)
    }

    fun getState() = state.asStateFlow()

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        state.value = ActivityVisibility.Visible
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        if (!activity.isFinishing && !activity.isChangingConfigurations) {
            state.value = ActivityVisibility.Invisible
        }
    }
}

sealed interface ActivityVisibility {

    object Initializing : ActivityVisibility
    object Invisible : ActivityVisibility
    object Visible : ActivityVisibility
}
