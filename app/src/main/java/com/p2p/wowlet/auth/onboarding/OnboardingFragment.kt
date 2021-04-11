package com.p2p.wowlet.auth.onboarding

import android.os.Bundle
import android.view.View
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.p2p.wowlet.R
import com.p2p.wowlet.auth.ui.RegLoginFragment
import com.p2p.wowlet.common.mvp.BaseFragment
import com.p2p.wowlet.databinding.FragmentOnboardingBinding
import com.p2p.wowlet.entities.local.SplashData
import com.p2p.wowlet.utils.popAndReplaceFragment
import com.p2p.wowlet.utils.viewbinding.viewBinding

class OnboardingFragment : BaseFragment(R.layout.fragment_onboarding) {
    private val binding: FragmentOnboardingBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            val listData = getStepsData()
            viewPager.adapter = OnboardingPagerAdapter(listData)
            pageIndicator.setViewPager(viewPager)

            viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {

                val lastPageIndex = listData.size - 1
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    if (position == lastPageIndex - 1) {
                        popAndReplaceFragment(RegLoginFragment(), inclusive = true)
                    }
                }
            })
        }
    }

    private fun getStepsData(): List<SplashData> =
        listOf(
            SplashData(
                resources.getString(R.string.splash_title),
                resources.getString(R.string.splash_hint)
            ),
            SplashData(
                resources.getString(R.string.splash_title),
                resources.getString(R.string.splash_hint)
            ),
            SplashData(
                resources.getString(R.string.splash_title),
                resources.getString(R.string.splash_hint)
            ),
            SplashData(
                resources.getString(R.string.splash_title),
                resources.getString(R.string.splash_hint)
            )
        )
}