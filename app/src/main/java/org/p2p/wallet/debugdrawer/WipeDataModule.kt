package org.p2p.wallet.debugdrawer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.p2p.wallet.databinding.ViewDebugDrawerWipeDataBinding
import io.palaima.debugdrawer.base.DebugModuleAdapter
import org.koin.core.component.KoinComponent

class WipeDataModule(
    private val restart: () -> Unit,
    private val clearData: () -> Unit
) : DebugModuleAdapter(), KoinComponent {

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup): View {
        val binding = ViewDebugDrawerWipeDataBinding.inflate(inflater, parent, false)
        binding.wipeData.setOnClickListener {
            clearData()
            restart()
        }

        return binding.root
    }
}
