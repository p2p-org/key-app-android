package org.p2p.wallet.restore.ui.derivable

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.solanaj.crypto.DerivationPath
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.ReserveMode.PIN_CODE
import org.p2p.wallet.auth.ui.pin.create.CreatePinFragment
import org.p2p.wallet.auth.ui.username.ReserveUsernameFragment
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentDerivableAccountsBinding
import org.p2p.wallet.restore.model.DerivableAccount
import org.p2p.wallet.restore.model.SecretKey
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

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
    }

    private val secretKeys: List<SecretKey> by args(EXTRA_SECRET_KEYS)
    override val presenter: DerivableAccountsContract.Presenter by inject {
        parametersOf(secretKeys)
    }
    private val accountsAdapter: DerivableAccountsAdapter by lazy {
        DerivableAccountsAdapter()
    }
    private val binding: FragmentDerivableAccountsBinding by viewBinding()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        analyticsInteractor.logScreenOpenEvent(ScreenNames.OnBoarding.DERIVATION)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            accountsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            accountsRecyclerView.adapter = accountsAdapter

            /* By default, we should create Bip44Change account */
            val allPaths = listOf(
                DerivationPath.BIP32DEPRECATED,
                DerivationPath.BIP44CHANGE,
                DerivationPath.BIP44
            )
            val pathAdapter = ArrayAdapter(
                requireContext(), R.layout.item_derivation_path, allPaths.map { it.stringValue }
            )
            val defaultPath = DerivationPath.BIP44CHANGE
            derivationPathTextView.setAdapter(pathAdapter)
            derivationPathTextView.setText(defaultPath.stringValue, false)
            derivationPathTextView.threshold = allPaths.indexOfFirst { defaultPath == it }

            binding.derivationPathTextView.setOnItemClickListener { _, _, position, _ ->
                presenter.setNewPath(allPaths[position])
            }

            restoreButton.setOnClickListener { presenter.createAndSaveAccount() }
        }

        presenter.loadData()
    }

    override fun showAccounts(accounts: List<DerivableAccount>) {
        accountsAdapter.setItems(accounts)
    }

    override fun navigateToCreatePin() {
        replaceFragment(CreatePinFragment.create())
    }

    override fun navigateToReserveUsername() {
        replaceFragment(ReserveUsernameFragment.create(PIN_CODE))
    }

    override fun showLoading(isLoading: Boolean) {
        binding.restoreButton.setLoading(isLoading)
        binding.accountsRecyclerView.isEnabled = !isLoading
        binding.derivationPathInputLayout.isEnabled = !isLoading
    }
}
