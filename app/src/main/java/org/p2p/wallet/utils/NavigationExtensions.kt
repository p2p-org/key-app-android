package org.p2p.wallet.utils

import androidx.annotation.AnimRes
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.whenStateAtLeast
import org.p2p.core.utils.hideKeyboard
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseFragmentContract
import kotlin.reflect.KClass
import kotlinx.coroutines.launch

inline fun LifecycleOwner.whenStateAtLeast(state: Lifecycle.State, crossinline block: () -> Unit) {
    if (lifecycle.currentState.isAtLeast(state)) {
        block()
    } else {
        lifecycle.coroutineScope.launch {
            lifecycle.whenStateAtLeast(state) { block() }
        }
    }
}

fun Fragment.backStackEntryCount(): Int = requireActivity().supportFragmentManager.backStackEntryCount

fun Fragment.popBackStack(hideKeyboard: Boolean = true) {
    if (hideKeyboard) requireActivity().hideKeyboard()
    requireActivity().popBackStack()
}

fun FragmentActivity.popBackStack() {
    if (supportFragmentManager.backStackEntryCount < 2) {
        finish()
    } else {
        supportFragmentManager.popBackStack()
    }
}

fun <T : Fragment> Fragment.popBackStackTo(
    target: KClass<T>,
    inclusive: Boolean = false,
    immediate: Boolean = true
): Boolean = requireActivity().popBackStackTo(target, inclusive, immediate)

fun <T : Fragment> FragmentActivity.popBackStackTo(
    target: KClass<T>,
    inclusive: Boolean = false,
    immediate: Boolean = true
): Boolean {
    val flag = if (inclusive) FragmentManager.POP_BACK_STACK_INCLUSIVE else 0

    hideKeyboard()

    val tag = target.java.name
    with(supportFragmentManager) {
        return if (backStackEntryCount == 0 || getBackStackEntryAt(0).name == tag && inclusive) {
            finish()
            true
        } else if (immediate) {
            popBackStackImmediate(tag, flag)
        } else {
            popBackStack(tag, flag)
            true
        }
    }
}

fun Fragment.popAndReplaceFragment(
    target: Fragment,
    popTo: KClass<out Fragment>? = null,
    @IdRes containerId: Int = R.id.rootContainer,
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
        if (this@popAndReplaceFragment is BaseFragmentContract) {
            this@popAndReplaceFragment.overrideExitAnimation(exit)
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

fun Fragment.addFragment(
    target: Fragment,
    @IdRes containerId: Int = R.id.rootContainer,
    addToBackStack: Boolean = true,
    @AnimRes enter: Int = R.anim.nav_enter,
    @AnimRes exit: Int = R.anim.nav_exit,
    @AnimRes popEnter: Int = R.anim.nav_pop_enter,
    @AnimRes popExit: Int = R.anim.nav_pop_exit,
    fragmentManager: FragmentManager = requireActivity().supportFragmentManager
) {
    whenStateAtLeast(Lifecycle.State.STARTED) {
        requireActivity().hideKeyboard()
        fragmentManager.commit(allowStateLoss = true) {
            setCustomAnimations(enter, exit, popEnter, popExit)
            add(containerId, target, target.javaClass.name)
            if (addToBackStack) addToBackStack(target.javaClass.name)
        }
    }
}

fun Fragment.replaceFragment(
    target: Fragment,
    @IdRes containerId: Int = R.id.rootContainer,
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

fun Fragment.replaceFragmentForResult(
    target: Fragment,
    requestKey: String,
    onResult: FragmentResultListener,
    @IdRes containerId: Int = R.id.rootContainer,
    addToBackStack: Boolean = true,
    @AnimRes enter: Int = R.anim.nav_enter,
    @AnimRes exit: Int = R.anim.nav_exit,
    @AnimRes popEnter: Int = R.anim.nav_pop_enter,
    @AnimRes popExit: Int = R.anim.nav_pop_exit,
    fragmentManager: FragmentManager = requireActivity().supportFragmentManager
) = whenStateAtLeast(Lifecycle.State.STARTED) {
    requireActivity().hideKeyboard()
    fragmentManager.setFragmentResultListener(requestKey, this, onResult)
    fragmentManager.commit(allowStateLoss = true) {
        setCustomAnimations(enter, exit, popEnter, popExit)
        replace(containerId, target, target.javaClass.name)
        if (addToBackStack) addToBackStack(target.javaClass.name)
    }
}

fun FragmentActivity.replaceFragment(
    target: Fragment,
    @IdRes containerId: Int = R.id.rootContainer,
    addToBackStack: Boolean = true
) = whenStateAtLeast(Lifecycle.State.STARTED) {
    hideKeyboard()
    supportFragmentManager.commit(allowStateLoss = true) {
        replace(containerId, target, target.javaClass.name)
        if (addToBackStack) addToBackStack(target.javaClass.name)
    }
}

fun FragmentActivity.addFragment(
    target: Fragment,
    @IdRes containerId: Int = R.id.rootContainer,
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
