package org.p2p.wallet.debugdrawer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.p2p.wallet.databinding.ViewDebugDrawerTimberBinding
import io.palaima.debugdrawer.base.DebugModuleAdapter

class CustomTimberModule : DebugModuleAdapter() {

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup): View {

        val binding = ViewDebugDrawerTimberBinding.inflate(inflater, parent, false)
        binding.showLogs.setOnClickListener {
            CustomLogDialog(parent.context).show()
        }

        return binding.root
    }
}
