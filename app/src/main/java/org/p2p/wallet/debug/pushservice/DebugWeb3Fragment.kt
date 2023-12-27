package org.p2p.wallet.debug.pushservice

import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import org.json.JSONObject
import org.koin.android.ext.android.inject
import java.nio.charset.Charset
import org.p2p.core.crypto.toBase64Instance
import org.p2p.core.utils.fromJsonReified
import org.p2p.uikit.utils.toast
import org.p2p.wallet.R
import org.p2p.wallet.auth.gateway.api.request.GatewayOnboardingMetadataCiphered
import org.p2p.wallet.auth.gateway.repository.mapper.GatewayServiceOnboardingMetadataCipher
import org.p2p.wallet.auth.interactor.MetadataChangesLogger
import org.p2p.wallet.auth.interactor.MetadataInteractor
import org.p2p.wallet.auth.model.MetadataLoadStatus
import org.p2p.wallet.auth.repository.UserSignUpDetailsStorage
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentDebugWeb3Binding
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.shareText
import org.p2p.wallet.utils.viewbinding.viewBinding

@SuppressLint("SetTextI18n")
class DebugWeb3Fragment : BaseFragment(R.layout.fragment_debug_web3) {

    companion object {
        fun create(): DebugWeb3Fragment = DebugWeb3Fragment()
    }

    private val binding: FragmentDebugWeb3Binding by viewBinding()
    private val metadataInteractor: MetadataInteractor by inject()
    private val signUpDetailsStorage: UserSignUpDetailsStorage by inject()
    private val metadataDecipher: GatewayServiceOnboardingMetadataCipher by inject()
    private val metadataChangesLogger: MetadataChangesLogger by inject()
    private val gson: Gson by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()

        with(binding) {
            buttonLoadMetadata.isVisible = !textViewMetadataValue.text.startsWith("{")
            buttonLoadMetadata.setOnClickListener {
                lifecycleScope.launchWhenResumed { loadMetadata() }
            }

            buttonDecipherMetadata.setOnClickListener {
                decipherMetadata()
            }
        }
    }

    private fun initView() = with(binding) {
        toolbar.setNavigationOnClickListener { popBackStack() }

        val web3AuthData = signUpDetailsStorage.getLastSignUpUserDetails()
        val web3DataJson = web3AuthData
            ?.toUiJson()
            ?: "None"

        val metadataJson = metadataInteractor.currentMetadata
            ?.toUiJson()
            ?: if (web3AuthData != null) "Not loaded" else "None"

        textViewWeb3Value.text = web3DataJson
        textViewMetadataValue.text = metadataJson

        buttonMetadataLogs.setOnClickListener {
            requireContext().shareText(metadataChangesLogger.logs.joinToString("\n"))
        }
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
            MetadataLoadStatus.NoWeb3EthereumPublicKey -> {
                binding.textViewMetadataValue.text = "No ETH public key found"
            }
            MetadataLoadStatus.Success -> {
                initView()
            }
        }
        binding.buttonLoadMetadata.setLoading(false)
    }

    private fun decipherMetadata() {
        val cipheredMetadata = binding.editTextDecipherMetadata.text?.toString()
            ?.trim()
            .orEmpty()
        if (cipheredMetadata.isNotBlank()) {
            // get json like GatewayOnboardingMetadataCiphered
            val metadataAsJson =
                kotlin.runCatching {
                    gson.fromJsonReified<GatewayOnboardingMetadataCiphered>(
                        cipheredMetadata.toBase64Instance()
                            .decodeToBytes()
                            .toString(Charset.defaultCharset())
                    )
                }
                    .onFailure { toast(it.toString()) }
                    .getOrNull()
            val seedPhrase = signUpDetailsStorage.getLastSignUpUserDetails()
                ?.signUpDetails
                ?.mnemonicPhraseWords

            if (seedPhrase != null && metadataAsJson != null) {
                kotlin.runCatching { metadataDecipher.decryptMetadata(seedPhrase, metadataAsJson) }
                    .onSuccess {
                        MaterialAlertDialogBuilder(requireContext())
                            .setMessage(it.toUiJson())
                            .setPositiveButton(R.string.common_ok) { d, _ ->
                                d.dismiss()
                            }
                            .setNegativeButton(R.string.common_share) { _, _ ->
                                requireContext().shareText(it.toUiJson())
                            }
                            .show()
                    }
            }
        }
    }

    private fun Any.toUiJson(): String {
        return JSONObject(gson.toJson(this)).toString(2)
    }
}
