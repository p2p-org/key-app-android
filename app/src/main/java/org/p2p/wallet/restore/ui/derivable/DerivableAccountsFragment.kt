package org.p2p.wallet.restore.ui.derivable

import android.os.Bundle
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.pin.create.CreatePinFragment
import org.p2p.wallet.auth.ui.pin.create.PinLaunchMode
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentDerivableAccountsBinding
import org.p2p.wallet.restore.model.DerivableAccount
import org.p2p.wallet.restore.model.SecretKey
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.solanaj.crypto.DerivationPath
import org.p2p.wallet.auth.ui.username.ReserveUsernameFragment
import org.p2p.wallet.utils.replaceFragment

class DerivableAccountsFragment :
    BaseMvpFragment<DerivableAccountsContract.View, DerivableAccountsContract.Presenter>(
        R.layout.fragment_derivable_accounts
    ),
    DerivableAccountsContract.View {

    companion object {
        private const val EXTRA_SECRET_KEYS = "EXTRA_SECRET_KEYS"
        fun create(secretKeys: List<SecretKey>) = DerivableAccountsFragment().withArgs(
            EXTRA_SECRET_KEYS to secretKeys
        )
        val TAG: String = DerivableAccountsFragment::class.java.simpleName
    }

    private val secretKeys: List<SecretKey> by args(EXTRA_SECRET_KEYS)

    override val presenter: DerivableAccountsContract.Presenter by inject {
        parametersOf(secretKeys)
    }

    private val accountsAdapter: DerivableAccountsAdapter by lazy {
        DerivableAccountsAdapter()
    }

    private val binding: FragmentDerivableAccountsBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            accountsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            accountsRecyclerView.adapter = accountsAdapter

            /* By default, we should create Bip44Change account */
            derivationPathTextView.text = DerivationPath.BIP44CHANGE.stringValue
            derivationPathTextView.setOnClickListener { presenter.loadCurrentPath() }
            restoreButton.setOnClickListener { presenter.createAndSaveAccount() }
        }

        presenter.loadData()
    }

    override fun showPathSelectionDialog(path: DerivationPath) {
        SelectDerivationPathDialog.show(childFragmentManager, path) {
            presenter.setNewPath(it)
        }
    }

    override fun showAccounts(path: DerivationPath, accounts: List<DerivableAccount>) {
        accountsAdapter.setItems(accounts)
        binding.derivationPathTextView.text = path.stringValue
    }

    override fun navigateToCreatePin() {
        popAndReplaceFragment(CreatePinFragment.create(PinLaunchMode.RECOVER), inclusive = true)
    }

    override fun navigateToReserveUsername() {
        replaceFragment(ReserveUsernameFragment.create(TAG))
    }

    override fun showLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.accountsRecyclerView.isInvisible = isLoading
        binding.restoreButton.isEnabled = !isLoading
        binding.derivationPathTextView.isEnabled = !isLoading
    }
}