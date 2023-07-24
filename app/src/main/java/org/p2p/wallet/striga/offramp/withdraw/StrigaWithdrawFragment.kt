package org.p2p.wallet.striga.offramp.withdraw

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.core.common.TextContainer
import org.p2p.uikit.components.edittext.v2.NewUiKitEditTextDrawableStrategy
import org.p2p.uikit.components.edittext.v2.NewUiKitEditTextMutator
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentStrigaOffRampWithdrawBinding
import org.p2p.wallet.striga.wallet.models.StrigaUserBankingDetails
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

class StrigaWithdrawFragment :
    BaseMvpFragment<StrigaWithdrawContract.View, StrigaWithdrawContract.Presenter>(
        R.layout.fragment_striga_off_ramp_withdraw
    ),
    StrigaWithdrawContract.View {

    companion object {
        private const val ARG_WITHDRAW_TYPE = "ARG_WITHDRAW_TYPE"

        fun create(type: StrigaWithdrawFragmentType): StrigaWithdrawFragment {
            return StrigaWithdrawFragment()
                .withArgs(ARG_WITHDRAW_TYPE to type)
        }
    }

    override val presenter: StrigaWithdrawContract.Presenter by inject()
    private val binding: FragmentStrigaOffRampWithdrawBinding by viewBinding()

    private val withdrawType: StrigaWithdrawFragmentType by args(ARG_WITHDRAW_TYPE)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { popBackStack() }

        binding.editTextIban.mutate()
            .setEndDrawableIsVisible(isVisible = false)
            .setDrawableStrategy(NewUiKitEditTextDrawableStrategy.SHOW_ON_TEXT)
            .setDrawableClickListener(NewUiKitEditTextMutator::clearInput)
            .addOnTextChangedListener { presenter.onIbanChanged(it.toString()) }

        binding.editTextBic.mutate()
            .setEndDrawableIsVisible(isVisible = false)
            .setDrawableStrategy(NewUiKitEditTextDrawableStrategy.SHOW_ON_TEXT)
            .setDrawableClickListener(NewUiKitEditTextMutator::clearInput)
            .addOnTextChangedListener { presenter.onBicChanged(it.toString()) }

        binding.buttonWithdraw.setOnClickListener {
            presenter.withdraw(withdrawType)
        }
    }

    override fun showLoading(isLoading: Boolean) {
        binding.buttonWithdraw.setLoading(isLoading)
    }

    override fun showPrefilledBankingDetails(details: StrigaUserBankingDetails) {
        details.bankingBic?.also { binding.editTextBic.mutate().setText(it) }
        details.bankingIban?.also { binding.editTextIban.mutate().setText(it) }
        binding.editTextReceiver.mutate().setText(details.bankingFullName)
    }

    override fun showIbanValidationResult(result: StrigaWithdrawValidationResult) {
        val error = result.errorTextRes?.let(TextContainer::invoke)
        binding.editTextIban.mutate().setError(error)
        binding.buttonWithdraw.isEnabled = error != null
    }

    override fun showBicValidationResult(result: StrigaWithdrawValidationResult) {
        val error = result.errorTextRes?.let(TextContainer::invoke)
        binding.editTextBic.mutate().setError(error)
        binding.buttonWithdraw.isEnabled = error != null
    }
}
