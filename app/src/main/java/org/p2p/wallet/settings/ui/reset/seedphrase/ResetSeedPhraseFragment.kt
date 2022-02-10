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
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentResetSeedPhraseBinding
import org.p2p.wallet.restore.model.SecretKey
import org.p2p.wallet.restore.ui.keys.adapter.SecretPhraseAdapter
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.attachAdapter
import org.p2p.wallet.utils.hideKeyboard
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val EXTRA_REQUEST_KEY = "EXTRA_REQUEST_KEY"
private const val EXTRA_RESULT_KEY = "EXTRA_RESULT_KEY"

class ResetSeedPhraseFragment :
    BaseMvpFragment<ResetSeedPhraseContract.View, ResetSeedPhraseContract.Presenter>(
        R.layout.fragment_reset_seed_phrase
    ),
    ResetSeedPhraseContract.View {

    override val presenter: ResetSeedPhraseContract.Presenter by inject()
    private val binding: FragmentResetSeedPhraseBinding by viewBinding()
    private val requestKey: String by args(EXTRA_REQUEST_KEY)
    private val resultKey: String by args(EXTRA_RESULT_KEY)

    companion object {
        fun create(requestKey: String, resultKey: String) = ResetSeedPhraseFragment()
            .withArgs(
                EXTRA_REQUEST_KEY to requestKey,
                EXTRA_RESULT_KEY to resultKey
            )
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
    }

    override fun showSuccess(secretKeys: List<SecretKey>) {
        setFragmentResult(requestKey, bundleOf(Pair(resultKey, secretKeys)))
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
}