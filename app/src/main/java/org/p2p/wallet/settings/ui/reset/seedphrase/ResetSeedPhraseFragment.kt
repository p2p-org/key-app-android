package org.p2p.wallet.settings.ui.reset.seedphrase

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import org.koin.android.ext.android.inject
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentResetSeedPhraseBinding
import org.p2p.wallet.restore.model.SecretKey
import org.p2p.wallet.restore.ui.keys.adapter.SecretPhraseAdapter
import org.p2p.wallet.utils.attachAdapter
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.hideKeyboard
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding

class ResetSeedPhraseFragment :
    BaseMvpFragment<ResetSeedPhraseContract.View, ResetSeedPhraseContract.Presenter>(
        R.layout.fragment_reset_seed_phrase
    ),
    ResetSeedPhraseContract.View {
    override val presenter: ResetSeedPhraseContract.Presenter by inject()
    private val binding: FragmentResetSeedPhraseBinding by viewBinding()

    companion object {
        const val REQUEST_KEY = "REQUEST_KEY_RESET_SEED_PHRASE"
        const val BUNDLE_SECRET_KEYS = "BUNDLE_KEY_SECRET_KEYS"
        fun create() = ResetSeedPhraseFragment()
    }

    private val phraseAdapter: SecretPhraseAdapter by lazy {
        SecretPhraseAdapter {
            presenter.setNewKeys(it)
            clearError()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener {
                popBackStack()
                it.hideKeyboard()
            }

            restoreButton.setOnClickListener {
                presenter.verifySeedPhrase()
            }

            keysRecyclerView.layoutManager = FlexboxLayoutManager(requireContext()).also {
                it.flexDirection = FlexDirection.ROW
                it.justifyContent = JustifyContent.FLEX_START
            }
            keysRecyclerView.attachAdapter(phraseAdapter)

            phraseTextView.setOnClickListener {
                phraseTextView.isVisible = false
                keysRecyclerView.isVisible = true
                phraseAdapter.addSecretKey(SecretKey())
            }
        }

        val itemsCount = phraseAdapter.itemCount
        setButtonEnabled(itemsCount != 0)
        copyPhrase()
    }

    override fun showSuccess(secretKeys: List<SecretKey>) {
        setFragmentResult(REQUEST_KEY, bundleOf(Pair(BUNDLE_SECRET_KEYS, secretKeys)))
        popBackStack()
    }

    override fun setButtonEnabled(isEnabled: Boolean) {
        binding.restoreButton.isEnabled = isEnabled
    }

    override fun showError(messageRes: Int) {
        binding.errorTextView.setText(messageRes)
        binding.messageTextView.isVisible = false
    }

    private fun clearError() {
        binding.errorTextView.text = ""
        binding.messageTextView.isVisible = true
    }

    private fun copyPhrase() {
        if (!BuildConfig.DEBUG) return
        requireContext().copyToClipBoard(
            listOf(
                "oval",
                "you",
                "token",
                "plug",
                "copper",
                "visa",
                "employ",
                "link",
                "sell",
                "asset",
                "kick",
                "sausage"
            ).joinToString(" ")
        )
    }
}