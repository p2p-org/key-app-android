package com.p2p.wowlet.utils

import androidx.annotation.AnimRes
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.whenStateAtLeast
import com.p2p.wowlet.R
import com.p2p.wowlet.common.BaseFragmentContract
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

inline fun LifecycleOwner.whenStateAtLeast(state: Lifecycle.State, crossinline block: () -> Unit) {
    if (lifecycle.currentState.isAtLeast(state)) {
        block()
    } else {
        lifecycle.coroutineScope.launch {
            lifecycle.whenStateAtLeast(state) { block() }
        }
    }
}

fun Fragment.popBackStack() {
    requireActivity().popBackStack()
}

fun FragmentActivity.popBackStack() {

    hideKeyboard()

    if (supportFragmentManager.backStackEntryCount < 2) {
        finish()
    } else {
        supportFragmentManager.popBackStackImmediate()
    }
}

fun <T : Fragment> Fragment.popBackStackTo(target: KClass<T>, inclusive: Boolean = false) {
    requireActivity().popBackStackTo(target, inclusive)
}

fun <T : Fragment> FragmentActivity.popBackStackTo(target: KClass<T>, inclusive: Boolean = false) {
    val flag = if (inclusive) FragmentManager.POP_BACK_STACK_INCLUSIVE else 0

    hideKeyboard()

    val tag = target.java.name
    with(supportFragmentManager) {
        if (backStackEntryCount == 0 || getBackStackEntryAt(0).name == tag && inclusive) {
            finish()
        } else {
            if (!popBackStackImmediate(tag, flag)) finish()
        }
    }
}

fun Fragment.popAndReplace(
    target: Fragment,
    popTo: KClass<out Fragment>? = null,
    @IdRes containerId: Int = R.id.content,
    addToBackStack: Boolean = true,
    inclusive: Boolean = false,
    @AnimRes enter: Int = R.anim.nav_enter,
    @AnimRes exit: Int = R.anim.nav_exit,
    @AnimRes popEnter: Int = R.anim.nav_pop_enter,
    @AnimRes popExit: Int = R.anim.nav_pop_exit,
    fragmentManager: FragmentManager = requireActivity().supportFragmentManager
) = whenStateAtLeast(Lifecycle.State.STARTED) {
    requireActivity().hideKeyboard()
    with(fragmentManager) {
        // Override exit animation for popping fragment
        if (this@popAndReplace is BaseFragmentContract) {
            this@popAndReplace.overrideExitAnimation(exit)
        }

        // Make pop entering fragment invisible during transition
        popTo?.java?.name
            ?.let { findFragmentByTag(it) as? BaseFragmentContract }
            ?.apply { overrideEnterAnimation(R.anim.nav_stay_transparent) }

        popBackStack(
            popTo?.java?.name,
            if (inclusive) FragmentManager.POP_BACK_STACK_INCLUSIVE else 0
        )

        commit(allowStateLoss = true) {
            // Preform immediate replace
            setCustomAnimations(enter, 0, popEnter, popExit)
            replace(containerId, target, target.javaClass.name)
            if (addToBackStack) addToBackStack(target.javaClass.name)
        }
    }
}

fun Fragment.add(
    target: Fragment,
    @IdRes containerId: Int = R.id.content,
    addToBackStack: Boolean = true,
    @AnimRes enter: Int = R.anim.nav_enter,
    @AnimRes exit: Int = R.anim.nav_exit,
    @AnimRes popEnter: Int = R.anim.nav_pop_enter,
    @AnimRes popExit: Int = R.anim.nav_pop_exit,
    fragmentManager: FragmentManager = requireActivity().supportFragmentManager
) = whenStateAtLeast(Lifecycle.State.STARTED) {
    requireActivity().hideKeyboard()
    fragmentManager.commit(allowStateLoss = true) {
        setCustomAnimations(enter, exit, popEnter, popExit)
        add(containerId, target, target.javaClass.name)
        if (addToBackStack) addToBackStack(target.javaClass.name)
    }
}

fun Fragment.replace(
    target: Fragment,
    @IdRes containerId: Int = R.id.content,
    addToBackStack: Boolean = true,
    @AnimRes enter: Int = R.anim.nav_enter,
    @AnimRes exit: Int = R.anim.nav_exit,
    @AnimRes popEnter: Int = R.anim.nav_pop_enter,
    @AnimRes popExit: Int = R.anim.nav_pop_exit,
    fragmentManager: FragmentManager = requireActivity().supportFragmentManager
) = whenStateAtLeast(Lifecycle.State.STARTED) {
    requireActivity().hideKeyboard()
    fragmentManager.commit(allowStateLoss = true) {
        setCustomAnimations(enter, exit, popEnter, popExit)
        replace(containerId, target, target.javaClass.name)
        if (addToBackStack) addToBackStack(target.javaClass.name)
    }
}

fun FragmentActivity.replace(
    target: Fragment,
    @IdRes containerId: Int = R.id.content,
    addToBackStack: Boolean = true
) = whenStateAtLeast(Lifecycle.State.STARTED) {
    hideKeyboard()
    supportFragmentManager.commit(allowStateLoss = true) {
        replace(containerId, target, target.javaClass.name)
        if (addToBackStack) addToBackStack(target.javaClass.name)
    }
}

fun FragmentActivity.add(
    target: Fragment,
    @IdRes containerId: Int = R.id.content,
    addToBackStack: Boolean = true,
    @AnimRes enter: Int = R.anim.nav_enter,
    @AnimRes exit: Int = R.anim.nav_exit,
    @AnimRes popEnter: Int = R.anim.nav_pop_enter,
    @AnimRes popExit: Int = R.anim.nav_pop_exit
) = whenStateAtLeast(Lifecycle.State.STARTED) {
    hideKeyboard()
    supportFragmentManager.commit(allowStateLoss = true) {
        setCustomAnimations(enter, exit, popEnter, popExit)
        add(containerId, target, target.javaClass.name)
        if (addToBackStack) addToBackStack(target.javaClass.name)
    }
}