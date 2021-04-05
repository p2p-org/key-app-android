package com.p2p.wowlet.fragment.splashscreen.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.p2p.wowlet.R
import com.p2p.wowlet.activity.MainActivity
import com.p2p.wowlet.appbase.FragmentBaseMVVM
import com.p2p.wowlet.appbase.utils.dataBinding
import com.p2p.wowlet.appbase.viewcommand.Command.*
import com.p2p.wowlet.appbase.viewcommand.ViewCommand
import com.p2p.wowlet.databinding.FragmentSplashScreenBinding
import com.p2p.wowlet.fragment.pincode.view.PinCodeFragment
import com.p2p.wowlet.fragment.reglogin.view.RegLoginFragment
import com.p2p.wowlet.fragment.splashscreen.viewmodel.SplashScreenViewModel
import com.p2p.wowlet.utils.popAndReplace
import com.wowlet.entities.enums.PinCodeFragmentType
import com.wowlet.entities.local.SplashData
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashScreenFragment :
    FragmentBaseMVVM<SplashScreenViewModel, FragmentSplashScreenBinding>() {
    private var lastPage = 0
    override val viewModel: SplashScreenViewModel by viewModel()
    override val binding: FragmentSplashScreenBinding by dataBinding(R.layout.fragment_splash_screen)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            viewModel = this@SplashScreenFragment.viewModel
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
    }

    override fun initView() {
        with(binding) {
            viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    if (position == lastPage) {
                        popAndReplace(RegLoginFragment(), inclusive = true)
                    }
                    Log.e("Selected_Page", position.toString())
                }
            })
        }
    }

    override fun processViewCommand(command: ViewCommand) {
        when (command) {
            is NavigateRegLoginViewCommand ->
                popAndReplace(RegLoginFragment(), inclusive = true)
            is NavigatePinCodeViewCommand ->
                popAndReplace(
                    target = PinCodeFragment.create(
                        openSplashScreen = true, isBackupDialog = false, type = PinCodeFragmentType.VERIFY
                    ),
                    inclusive = true
                )
            is OpenMainActivityViewCommand -> {
                activity?.let {
                    val intent = Intent(it, MainActivity::class.java)
                    it.startActivity(intent)
                    it.finish()
                }
            }
        }
    }
}