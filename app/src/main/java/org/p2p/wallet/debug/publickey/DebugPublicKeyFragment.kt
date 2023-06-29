package org.p2p.wallet.debug.publickey

import androidx.core.widget.doAfterTextChanged
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.core.utils.Base58Utils
import org.p2p.uikit.utils.setTextColorRes
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentDebugPublicKeyBinding
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.infrastructure.security.SecureStorageContract
import org.p2p.core.crypto.Base58String
import org.p2p.wallet.utils.getClipboardText
import org.p2p.wallet.utils.popBackStack
import org.p2p.core.crypto.toBase58Instance
import org.p2p.wallet.utils.viewbinding.viewBinding

class DebugPublicKeyFragment : BaseFragment(R.layout.fragment_debug_public_key) {
    companion object {
        fun create(): DebugPublicKeyFragment = DebugPublicKeyFragment()
    }

    private val binding: FragmentDebugPublicKeyBinding by viewBinding()

    private val tokenKeyProvider: TokenKeyProvider by inject()
    private val secureStorageContract: SecureStorageContract by inject()

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val actualKey = getActualPublicKey()
        val stubKey = getStubPublicKey()

        val currentlyUsedKey = tokenKeyProvider.publicKey.takeIf(String::isNotBlank)?.toBase58Instance()

        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }

            textViewActualKeyValue.text = actualKey?.base58Value ?: "-"
            textViewMockKeyValue.text = stubKey?.base58Value ?: "-"

            when (currentlyUsedKey) {
                actualKey -> {
                    textViewActualKeyValue.setTextColorRes(R.color.text_mint)
                }
                stubKey -> {
                    textViewMockKeyValue.setTextColorRes(R.color.text_mint)
                }
            }
            buttonUseStubPublicKey.isEnabled = stubKey?.base58Value != null
            buttonUseActualPublicKey.isEnabled = actualKey?.base58Value != null

            editTextMockKey.doAfterTextChanged {
                val newMockedKey = it?.toString().orEmpty()
                if (Base58Utils.isValidBase58(newMockedKey)) {
                    buttonUseStubPublicKey.isEnabled = true
                    buttonUseStubPublicKey.text = "Switch to stub public key"
                } else {
                    buttonUseStubPublicKey.isEnabled = false
                    buttonUseStubPublicKey.text = "Invalid format for stub public key"
                }
            }

            textViewPaste.setOnClickListener {
                val clipboardValue = requireContext().getClipboardText(trimmed = true)
                if (!clipboardValue.isNullOrBlank()) {
                    editTextMockKey.setText(clipboardValue)
                }
            }

            buttonUseActualPublicKey.setOnClickListener {
                tokenKeyProvider.useStubKey = false
                popBackStack()
            }

            buttonUseStubPublicKey.setOnClickListener {
                tokenKeyProvider.useStubKey = true
                tokenKeyProvider.publicKey =
                    editTextMockKey.text.toString().takeIf(String::isNotBlank) ?: tokenKeyProvider.publicKey
                popBackStack()
            }
        }
    }

    private fun getActualPublicKey(): Base58String? {
        return runCatching { secureStorageContract.getString(SecureStorageContract.Key.KEY_PUBLIC_KEY) }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?.let { Base58Utils.decodeToString(it) }
            ?.toBase58Instance()
    }

    private fun getStubPublicKey(): Base58String? {
        return runCatching { secureStorageContract.getString(SecureStorageContract.Key.KEY_STUB_PUBLIC_KEY) }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?.let { Base58Utils.decodeToString(it) }
            ?.toBase58Instance()
    }
}
