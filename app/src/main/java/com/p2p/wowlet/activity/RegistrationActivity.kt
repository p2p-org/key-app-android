package com.p2p.wowlet.activity

import android.os.Bundle
import android.util.Log

import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.p2p.wowlet.R
import com.wowlet.data.util.mnemoticgenerator.English
import com.wowlet.data.util.mnemoticgenerator.MnemonicGenerator
import com.wowlet.data.util.mnemoticgenerator.Words

import java.security.SecureRandom


class RegistrationActivity : AppCompatActivity() {
    lateinit var navHostFragment: NavHostFragment
    val nav by lazy { findNavController(R.id.registrationContainer) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.registrationContainer) as NavHostFragment
    }
}