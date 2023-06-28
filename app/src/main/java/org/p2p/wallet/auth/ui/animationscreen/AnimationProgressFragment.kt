package org.p2p.wallet.auth.ui.animationscreen

import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import android.os.Bundle
import android.view.View
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentAnimationProgressBinding
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_TIMER_STATE_LIST = "ARG_TIMER_STATE_LIST"

class AnimationProgressFragment : BaseFragment(R.layout.fragment_animation_progress) {

    companion object {
        // TODO make this AnimationProgressFragment smarter and get rid from isAnimating flag
        private val fragmentTag = AnimationProgressFragment::class.java.name
        private var isAnimating = false

        private fun create(timerStateList: List<TimerState>) =
            AnimationProgressFragment().withArgs(ARG_TIMER_STATE_LIST to timerStateList)

        fun show(fragmentManager: FragmentManager, isCreation: Boolean) {
            val loadingTitleRes = if (isCreation) {
                R.string.onboarding_loading_creating_title
            } else {
                R.string.onboarding_loading_recovery_title
            }
            show(
                fragmentManager = fragmentManager,
                timerStateList = listOf(
                    TimerState(loadingTitleRes, withProgress = false),
                    TimerState(loadingTitleRes),
                    TimerState(R.string.onboarding_loading_title_2),
                    TimerState(R.string.onboarding_loading_title_3),
                )
            )
        }

        fun show(fragmentManager: FragmentManager, timerStateList: List<TimerState>) {
            if (!isAnimating) {
                isAnimating = true
                fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    .add(
                        android.R.id.content,
                        create(timerStateList),
                        fragmentTag
                    )
                    .commit()
            }
        }

        fun dismiss(fragmentManager: FragmentManager) {
            fragmentManager.findFragmentByTag(fragmentTag)?.let { fragment ->
                fragmentManager.beginTransaction()
                    .remove(fragment)
                    .commit()
                isAnimating = false
            }
        }
    }

    private val binding: FragmentAnimationProgressBinding by viewBinding()

    private val timerStateList: List<TimerState> by args(ARG_TIMER_STATE_LIST)

    private var creationProgressJob: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLoadingAnimationState(isScreenLoading = true)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // ignore back pressing on this dialog
        }
    }

    private fun setLoadingAnimationState(isScreenLoading: Boolean) {
        with(binding) {
            TransitionManager.beginDelayedTransition(root)
            loadingAnimationView.isVisible = isScreenLoading
            animationView.apply {
                if (isScreenLoading) {
                    startCreationProgressJob()
                    playAnimation()
                } else {
                    creationProgressJob?.cancel()
                    cancelAnimation()
                }
            }
        }
    }

    private fun startCreationProgressJob() {
        creationProgressJob = timerStateList.asSequence()
            .asFlow()
            .onEach {
                with(binding) {
                    textViewCreationTitle.setText(it.titleRes)
                    textViewCreationMessage.isVisible = !it.withProgress
                    progressBarCreation.isVisible = it.withProgress
                }
                delay(2.seconds.inWholeMilliseconds)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onDestroyView() {
        creationProgressJob?.cancel()
        super.onDestroyView()
    }
}
