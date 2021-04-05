package com.p2p.wowlet.auth

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.postDelayed
import com.p2p.wowlet.R
import com.p2p.wowlet.fragment.pincode.view.PinCodeFragment
import com.p2p.wowlet.fragment.splashscreen.view.SplashScreenFragment
import com.p2p.wowlet.utils.replaceFragment
import com.wowlet.domain.usecases.AuthInteractor
import com.wowlet.entities.enums.PinCodeFragmentType
import org.koin.android.ext.android.inject

// fixme: remove activity, and leave only [MainActivity] - we are working on single activity architecture
class RegistrationActivity : AppCompatActivity() {

    private val authInteractor: AuthInteractor by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        Handler(Looper.myLooper()!!).postDelayed(500L) {
            if (savedInstanceState == null) {
                if (authInteractor.isAuthorized()) {
                    replaceFragment(
                        PinCodeFragment.create(
                            openSplashScreen = true,
                            isBackupDialog = false,
                            type = PinCodeFragmentType.VERIFY
                        )
                    )
                } else {
                    replaceFragment(SplashScreenFragment())
                }
            }
        }
    }
}