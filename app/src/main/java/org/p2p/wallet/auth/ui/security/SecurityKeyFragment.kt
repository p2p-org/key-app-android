package org.p2p.wallet.auth.ui.security

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.username.ReserveUsernameFragment
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSecurityKeyBinding
import org.p2p.wallet.utils.attachAdapter
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.edgetoedge.Edge
import org.p2p.wallet.utils.edgetoedge.edgeToEdge
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding

class SecurityKeyFragment :
    BaseMvpFragment<SecurityKeyContract.View, SecurityKeyContract.Presenter>(R.layout.fragment_security_key),
    SecurityKeyContract.View {

    companion object {
        fun create() = SecurityKeyFragment()
        val TAG: String = SecurityKeyFragment::class.java.simpleName
    }

    override val presenter: SecurityKeyContract.Presenter by inject()

    private val binding: FragmentSecurityKeyBinding by viewBinding()

    private val keysAdapter: KeysAdapter by lazy {
        KeysAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            edgeToEdge {
                toolbar.fit { Edge.TopArc }
                nextButton.fitMargin { Edge.BottomArc }
            }
            toolbar.setNavigationOnClickListener { popBackStack() }
            toolbar.inflateMenu(R.menu.menu_security_key)
            toolbar.setOnMenuItemClickListener {
                presenter.loadKeys()
                true
            }

            savedCheckBox.setOnCheckedChangeListener { _, isChecked ->
                nextButton.isEnabled = isChecked
            }
            nextButton.setOnClickListener {
                presenter.createAccount()
            }

            copyTextView.setOnClickListener {
                presenter.copyKeys()
            }

            with(keysRecyclerView) {
                attachAdapter(keysAdapter)
                layoutManager = FlexboxLayoutManager(requireContext()).also {
                    it.flexDirection = FlexDirection.ROW
                    it.justifyContent = JustifyContent.FLEX_START
                }
            }
        }

        presenter.loadKeys()
    }

    override fun showKeys(keys: List<String>) {
        keysAdapter.setItems(keys)
    }

    override fun navigateToCreatePin() {
        replaceFragment(ReserveUsernameFragment.create(TAG))
    }

    override fun showLoading(isLoading: Boolean) {
        binding.progressView.isVisible = isLoading
    }

    override fun copyToClipboard(keys: List<String>) {
        if (keys.isNotEmpty()) {
            val data = keys.joinToString(separator = " ")
            requireContext().copyToClipBoard(data)
            binding.copyTextView.setText(R.string.auth_copied)
        }
    }
}