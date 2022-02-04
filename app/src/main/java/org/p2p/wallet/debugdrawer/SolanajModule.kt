package org.p2p.wallet.debugdrawer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import io.palaima.debugdrawer.base.DebugModuleAdapter
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.databinding.ViewDebugDrawerSolanajModuleBinding
import org.p2p.wallet.rpc.interactor.CloseInteractor
import timber.log.Timber

class SolanajModule : DebugModuleAdapter(), KoinComponent {

    private val closeInteractor: CloseInteractor by inject()
    private val appScope: AppScope by inject()

    private lateinit var binding: ViewDebugDrawerSolanajModuleBinding

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup): View {
        binding = ViewDebugDrawerSolanajModuleBinding.inflate(inflater, parent, false)

        with(binding) {
            closeAccountButton.setOnClickListener {
                val addressToClose = binding.addressEditText.text.toString()
                closeAccount(addressToClose)
            }
        }
        return binding.root
    }

    private fun closeAccount(addressToClose: String) {
        if (addressToClose.isBlank()) return

        appScope.launch {
            try {
                showLoading(true)
                closeInteractor.close(addressToClose)
                binding.addressEditText.text.clear()
                Toast.makeText(binding.root.context, "Account successfully closed!", Toast.LENGTH_SHORT).show()
            } catch (e: Throwable) {
                Timber.e(e, "Error closing account: $addressToClose")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.closeAccountButton.isEnabled = !isLoading
    }
}