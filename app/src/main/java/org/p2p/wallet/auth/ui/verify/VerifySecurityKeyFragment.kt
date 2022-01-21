package org.p2p.wallet.auth.ui.verify

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.ReserveMode
import org.p2p.wallet.auth.ui.username.ReserveUsernameFragment
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentVerifySecurityKeyBinding
import org.p2p.wallet.utils.withArgs
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.attachAdapter
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.edgetoedge.Edge
import org.p2p.wallet.utils.edgetoedge.edgeToEdge
import org.p2p.wallet.utils.viewbinding.viewBinding

private const val EXTRA_KEYS = "EXTRA_KEYS"

class VerifySecurityKeyFragment :
    BaseMvpFragment<VerifySecurityKeyContract.View, VerifySecurityKeyContract.Presenter>(
        R.layout.fragment_verify_security_key
    ),
    VerifySecurityKeyContract.View {

    companion object {
        fun create(selectedKeys: List<String>) = VerifySecurityKeyFragment().withArgs(
            EXTRA_KEYS to selectedKeys
        )
    }

    override val presenter: VerifySecurityKeyPresenter by inject()
    private val binding: FragmentVerifySecurityKeyBinding by viewBinding()
    private val adapter = VerifySecurityKeyAdapter { keyIndex, key ->
        presenter.onKeySelected(keyIndex, key)
    }
    private val keys: List<String> by args(EXTRA_KEYS)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            edgeToEdge {
                toolbar.fit { Edge.TopArc }
                progressButton.fitMargin { Edge.BottomArc }
            }
            progressButton.setOnClickListener {
                presenter.validate()
            }
            toolbar.setNavigationOnClickListener { popBackStack() }
            keysRecyclerView.attachAdapter(adapter)
        }
        presenter.load(keys)
    }

    override fun showKeys(keys: List<SecurityKeyTuple>) {
        adapter.setItems(keys)
    }

    override fun showLoading(isLoading: Boolean) {
        binding.progressView.isVisible = isLoading
    }

    override fun navigateToReserve() {
        replaceFragment(ReserveUsernameFragment.create(ReserveMode.PIN_CODE))
    }

    override fun showKeysDoesNotMatchError() {
        AlertDialog.Builder(requireContext(), R.style.WalletTheme_AlertDialog)
            .setTitle(R.string.auth_words_does_not_match_title)
            .setMessage(R.string.auth_words_does_not_match_message)
            .setNegativeButton(R.string.common_go_back) { _, _ ->
                popBackStack()
            }
            .setPositiveButton(R.string.common_try_again) { _, _ ->
                presenter.retry()
            }
            .show()
    }

    override fun onCleared() {
        presenter.load(selectedKeys = keys, shuffle = true)
    }

    override fun showEnabled(isEnable: Boolean) {
        binding.progressButton.isEnabled = isEnable
    }
}