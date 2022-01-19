package org.p2p.wallet.auth.ui.verify

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentVerifySecurityKeyBinding
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.attachAdapter
import org.p2p.wallet.utils.edgetoedge.Edge
import org.p2p.wallet.utils.edgetoedge.edgeToEdge
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

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
}