package org.p2p.wallet.restore.ui.derivable

import androidx.recyclerview.widget.LinearLayoutManager
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.solanaj.crypto.DerivationPath
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.pin.newcreate.NewCreatePinFragment
import org.p2p.core.analytics.constants.ScreenNames
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentDerivableAccountsBinding
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.restore.model.DerivableAccount
import org.p2p.wallet.restore.ui.derivable.bottomsheet.SelectDerivableAccountBottomSheet
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.getSerializableCompat
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.unsafeLazy
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val KEY_RESULT_PATH = "KEY_RESULT_PATH"
private const val KEY_REQUEST_PATH = "KEY_REQUEST_PATH"

class DerivableAccountsFragment :
    BaseMvpFragment<DerivableAccountsContract.View, DerivableAccountsContract.Presenter>(
        R.layout.fragment_derivable_accounts
    ),
    DerivableAccountsContract.View {

    companion object {
        private const val EXTRA_SECRET_KEYS = "EXTRA_SECRET_KEYS"
        fun create(secretKeys: List<String>) = DerivableAccountsFragment().withArgs(
            EXTRA_SECRET_KEYS to secretKeys
        )
    }

    private val secretKeys: List<String> by args(EXTRA_SECRET_KEYS)

    override val presenter: DerivableAccountsContract.Presenter by inject { parametersOf(secretKeys) }

    private val accountsAdapter: DerivableAccountsAdapter by unsafeLazy {
        DerivableAccountsAdapter(
            onAccountClick = presenter::createAndSaveAccount
        )
    }

    private val binding: FragmentDerivableAccountsBinding by viewBinding()

    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()

    private var selectedPath: DerivationPath = DerivationPath.BIP44CHANGE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        analyticsInteractor.logScreenOpenEvent(ScreenNames.OnBoarding.DERIVATION)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            toolbar.setOnMenuItemClickListener {
                if (it.itemId == R.id.helpItem) {
                    // pass empty string as UserId to launch IntercomService as anonymous user
                    IntercomService.signIn(userId = emptyString())
                    IntercomService.showMessenger()
                    return@setOnMenuItemClickListener true
                }
                false
            }
            recyclerViewAccounts.layoutManager = LinearLayoutManager(requireContext())
            recyclerViewAccounts.adapter = accountsAdapter

            /* By default, we should create Bip44Change account */
            viewDerivationPath.textViewSubtitle.text = selectedPath.stringValue

            viewDerivationPath.root.setOnClickListener {
                SelectDerivableAccountBottomSheet.show(
                    fm = childFragmentManager,
                    currentPath = selectedPath,
                    requestKey = KEY_REQUEST_PATH,
                    resultKey = KEY_RESULT_PATH
                )
            }

            buttonRestore.setOnClickListener { presenter.createAndSaveAccount() }
        }

        presenter.loadData()

        childFragmentManager.setFragmentResultListener(
            KEY_REQUEST_PATH,
            viewLifecycleOwner,
            ::onFragmentResult
        )
    }

    override fun showAccounts(accounts: List<DerivableAccount>) {
        accountsAdapter.setItems(accounts)
    }

    override fun navigateToCreatePin() {
        replaceFragment(NewCreatePinFragment.create())
    }

    override fun showLoading(isLoading: Boolean) {
        binding.buttonRestore.setLoading(isLoading)

        binding.buttonRestore.isEnabled = !isLoading
        binding.recyclerViewAccounts.isEnabled = !isLoading
        binding.textInputLayoutDerivationPath.isEnabled = !isLoading
    }

    private fun onFragmentResult(requestKey: String, result: Bundle) {
        when (requestKey) {
            KEY_REQUEST_PATH -> {
                result.getSerializableCompat<DerivationPath>(KEY_RESULT_PATH)?.let {
                    presenter.setNewPath(it)
                    selectedPath = it
                    binding.viewDerivationPath.textViewSubtitle.text = it.stringValue
                }
            }
        }
    }
}
