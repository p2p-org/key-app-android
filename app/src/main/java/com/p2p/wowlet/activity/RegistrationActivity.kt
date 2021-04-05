package com.p2p.wowlet.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.p2p.wowlet.R
import com.p2p.wowlet.fragment.splashscreen.view.SplashScreenFragment
import com.p2p.wowlet.utils.replace

class RegistrationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        if (savedInstanceState == null) {
            replace(SplashScreenFragment())
        }
    }
}