package com.p2p.wowlet.fragment.splashscreen.view

import android.os.Bundle
import android.view.View
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.p2p.wowlet.R
import com.p2p.wowlet.common.mvp.BaseFragment
import com.p2p.wowlet.databinding.FragmentSplashScreenBinding
import com.p2p.wowlet.fragment.splashscreen.adapters.PagerAdapter
import com.p2p.wowlet.fragment.splashscreen.viewmodel.SplashScreenViewModel
import com.p2p.wowlet.utils.viewbinding.viewBinding
import com.p2p.wowlet.entities.local.SplashData
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashScreenFragment : BaseFragment(R.layout.fragment_splash_screen) {
    private var lastPage = 0
    private val viewModel: SplashScreenViewModel by viewModel()
    private val binding: FragmentSplashScreenBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            viewModel.pages.observe(viewLifecycleOwner) {
                viewPager.adapter = PagerAdapter(it)
                pageIndicator.setViewPager(viewPager)
            }
        }
        val list = listOf(
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
        lastPage = list.size - 1
        viewModel.initData(list)

        initView()
    }

    private fun initView() {
        with(binding) {
            viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    if (position == lastPage) {
                        popAndReplaceFragment(OnboardingFragment(), inclusive = true)
                    }
                    Log.e("Selected_Page", position.toString())
                }
            })
        }
    }
}