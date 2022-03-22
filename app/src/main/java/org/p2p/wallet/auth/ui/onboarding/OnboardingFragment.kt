package org.p2p.wallet.auth.ui.onboarding

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.annotation.RawRes
import androidx.core.view.isVisible
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.analytics.OnBoardingAnalytics
import org.p2p.wallet.auth.ui.createwallet.CreateWalletFragment
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentOnboardingBinding
import org.p2p.wallet.restore.ui.keys.SecretKeyFragment
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding

class OnboardingFragment : BaseFragment(R.layout.fragment_onboarding) {

    companion object {
        fun create() = OnboardingFragment()
    }

    private val binding: FragmentOnboardingBinding by viewBinding()
    private val analytics: OnBoardingAnalytics by inject()

    private var isFinalAnimationWorking = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            prepareAnimationAndStart()
            createButton.clipToOutline = true
            createButton.setOnClickListener {
                analytics.logSplashCreating()
                runAfterAnimation { replaceFragment(CreateWalletFragment.create()) }
            }
            loginButton.setOnClickListener {
                analytics.logSplashRestored()
                runAfterAnimation { replaceFragment(SecretKeyFragment.create()) }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        binding.animationVideoView.pause()
    }

    override fun onStop() {
        super.onStop()
        binding.animationVideoViewPlaceHolder.isVisible = true
        isFinalAnimationWorking = false
    }

    override fun onResume() {
        super.onResume()
        prepareAnimationAndStart()
    }

    private fun prepareAnimationAndStart() {
        with(binding) {
            animationVideoView.apply {
                setVideoURI(getVideoUriFromResources(R.raw.anim1_white))
                setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.setOnInfoListener { _, infoState, _ ->
                        if (infoState == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                            animationVideoViewPlaceHolder.isVisible = false
                        }
                        false
                    }
                    mediaPlayer.isLooping = true
                }
                start()
            }
        }
    }

    private fun runAfterAnimation(transaction: () -> Unit) {
        if (!isFinalAnimationWorking) {
            binding.apply {
                animationVideoView.apply {
                    setVideoURI(getVideoUriFromResources(R.raw.anim2_white))
                    setOnPreparedListener { mediaPlayer ->
                        mediaPlayer.isLooping = false
                    }
                    setOnCompletionListener {
                        isFinalAnimationWorking = false
                        transaction.invoke()
                    }
                    start()
                    isFinalAnimationWorking = true
                }
            }
        }
    }

    // TODO P2PW-583 support dark theme to load assets for dark theme mode
    private fun getVideoUriFromResources(@RawRes animRes: Int): Uri = Uri.parse(
        "android.resource://" +
            requireContext().packageName.toString() +
            "/" + animRes
    )
}
