package org.p2p.uikit.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import org.p2p.uikit.sample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        if (supportFragmentManager.findFragmentById(R.id.rootView) == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.rootView, FinanceBlockFragment())
                .commit()
        }
    }
}