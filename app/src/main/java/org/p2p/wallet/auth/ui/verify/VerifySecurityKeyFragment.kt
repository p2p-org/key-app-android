package org.p2p.wallet.auth.ui.verify

import androidx.core.view.isVisible
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.uikit.utils.attachAdapter
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.pin.newcreate.NewCreatePinFragment
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentVerifySecurityKeyBinding
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.showInfoDialog
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val EXTRA_KEYS = "EXTRA_KEYS"

@Deprecated("Old onboarding flow, delete someday")
class VerifySecurityKeyFragment :
    BaseMvpFragment<VerifySecurityKeyContract.View, VerifySecurityKeyContract.Presenter>(
        R.layout.fragment_verify_security_key
    ),
    VerifySecurityKeyContract.View {

    companion object {
        fun create(selectedKeys: List<String>): VerifySecurityKeyFragment = VerifySecurityKeyFragment().withArgs(
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
            progressButton.setOnClickListener {
                presenter.validateSecurityKey()
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
        replaceFragment(NewCreatePinFragment.create())
        // skip this step due to PWN-4377
        // replaceFragment(ReserveUsernameFragment.create(ReserveMode.PIN_CODE))
    }

    override fun showKeysDoesNotMatchError() {
        showInfoDialog(
            titleRes = R.string.auth_words_does_not_match_title,
            messageRes = R.string.auth_words_does_not_match_message,
            primaryButtonRes = R.string.common_try_again,
            secondaryButtonRes = R.string.common_go_back,
            primaryCallback = { presenter.retry() },
            secondaryCallback = { popBackStack() }
        )
    }

    override fun onCleared() {
        presenter.load(selectedKeys = keys, shuffle = true)
    }

    override fun showEnabled(isEnable: Boolean) {
        with(binding.progressButton) {
            if (isEnable) {
                setActionText(R.string.auth_save_and_continue)
                setDrawableEnd(R.drawable.ic_next)
            } else {
                setActionText(R.string.auth_choose_correct_words)
                setDrawableEnd(null)
            }
        }
        binding.progressButton.isEnabled = isEnable
    }
}
