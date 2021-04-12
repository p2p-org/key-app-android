package com.p2p.wallet.auth.ui

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import com.p2p.wallet.R
import com.p2p.wallet.auth.ui.pincode.view.PinCodeFragment
import com.p2p.wallet.common.mvp.BaseFragment
import com.p2p.wallet.databinding.FragmentRegWalletBinding
import com.p2p.wallet.auth.model.LaunchMode
import com.p2p.wallet.utils.popBackStack
import com.p2p.wallet.utils.replaceFragment
import com.p2p.wallet.utils.viewbinding.viewBinding

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
                        type = LaunchMode.CREATE
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