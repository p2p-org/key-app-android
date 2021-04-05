package com.p2p.wowlet.fragment.regwallet.view

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import com.p2p.wowlet.R
import com.p2p.wowlet.common.mvp.BaseFragment
import com.p2p.wowlet.databinding.FragmentRegWalletBinding
import com.p2p.wowlet.fragment.pincode.view.PinCodeFragment
import com.p2p.wowlet.utils.popBackStack
import com.p2p.wowlet.utils.replaceFragment
import com.p2p.wowlet.utils.viewbinding.viewBinding
import com.wowlet.entities.enums.PinCodeFragmentType

class RegWalletFragment : BaseFragment(R.layout.fragment_reg_wallet) {

    private lateinit var yourCountDownTimer: CountDownTimer
    private val binding: FragmentRegWalletBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.backImageView.setOnClickListener { popBackStack() }
        yourCountDownTimer = object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                replaceFragment(
                    PinCodeFragment.create(
                        openSplashScreen = false,
                        isBackupDialog = false,
                        type = PinCodeFragmentType.CREATE
                    )
                )
            }
        }.start()
    }

    override fun onPause() {
        super.onPause()
        yourCountDownTimer.cancel()
    }
}