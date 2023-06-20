package org.p2p.wallet.debug.pushservice

import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.google.gson.Gson
import org.json.JSONObject
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.MetadataInteractor
import org.p2p.wallet.auth.model.MetadataLoadStatus
import org.p2p.wallet.auth.repository.UserSignUpDetailsStorage
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentDebugWeb3Binding
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding

@SuppressLint("SetTextI18n")
class DebugWeb3Fragment : BaseFragment(R.layout.fragment_debug_web3) {

    companion object {
        fun create(): DebugWeb3Fragment = DebugWeb3Fragment()
    }

    private val binding: FragmentDebugWeb3Binding by viewBinding()
    private val metadataInteractor: MetadataInteractor by inject()
    private val signUpDetailsStorage: UserSignUpDetailsStorage by inject()
    private val gson: Gson by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()

        with(binding) {
            buttonLoadMetadata.isVisible = !textViewMetadataValue.text.startsWith("{")
            buttonLoadMetadata.setOnClickListener {
                lifecycleScope.launchWhenResumed { loadMetadata() }
            }
        }
    }

    private fun initView() = with(binding) {
        toolbar.setNavigationOnClickListener { popBackStack() }

        val web3AuthData = signUpDetailsStorage.getLastSignUpUserDetails()
        val web3DataJson = web3AuthData
            ?.let { JSONObject(gson.toJson(it)).toString(2) }
            ?: "None"

        val metadataJson = metadataInteractor.currentMetadata
            ?.let { JSONObject(gson.toJson(it)).toString(2) }
            ?: if (web3AuthData != null) "Not loaded" else "None"

        textViewWeb3Value.text = web3DataJson
        textViewMetadataValue.text = metadataJson
    }

    private suspend fun loadMetadata() {
        binding.buttonLoadMetadata.setLoading(true)
        when (val result = metadataInteractor.tryLoadAndSaveMetadata()) {
            MetadataLoadStatus.Canceled -> {
                binding.textViewMetadataValue.text = "Cancelled"
            }
            is MetadataLoadStatus.Failure -> {
                binding.textViewMetadataValue.text = buildString {
                    var cause: Throwable? = result.cause
                    while (cause != null) {
                        append("$cause")
                        appendLine()
                        appendLine()
                        cause = cause.cause
                    }
                }
            }
            MetadataLoadStatus.NoEthereumPublicKey -> {
                binding.textViewMetadataValue.text = "No ETH public key found"
            }
            MetadataLoadStatus.Success -> {
                initView()
            }
        }
        binding.buttonLoadMetadata.setLoading(false)
    }
}
