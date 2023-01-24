package org.p2p.wallet.auth.ui.animationscreen

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
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
import kotlin.time.Duration.Companion.seconds

private const val ARG_TITLE_RES = "ARG_TITLE_RES"

class AnimationProgressFragment : BaseFragment(R.layout.fragment_animation_progress) {

    companion object {
        // TODO make this AnimationProgressFragment smarter and get rid from isAnimating flag
        private val fragmentTag = AnimationProgressFragment::class.java.name
        private var isAnimating = false

        private fun create(loadingTitleRes: Int) =
            AnimationProgressFragment().withArgs(ARG_TITLE_RES to loadingTitleRes)

        fun show(fragmentManager: FragmentManager, isCreation: Boolean) {
            if (!isAnimating) {
                isAnimating = true
                val loadingTitleRes = if (isCreation) {
                    R.string.onboarding_loading_creating_title
                } else {
                    R.string.onboarding_loading_recovery_title
                }
                fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    .add(
                        android.R.id.content,
                        create(loadingTitleRes),
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

    private val loadingTitleRes: Int by args(ARG_TITLE_RES)

    private var creationProgressJob: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLoadingAnimationState(isScreenLoading = true)
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
        creationProgressJob = listOf(
            TimerState(loadingTitleRes, withProgress = false),
            TimerState(loadingTitleRes),
            TimerState(R.string.onboarding_loading_title_2),
            TimerState(R.string.onboarding_loading_title_3),
        ).asSequence()
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
