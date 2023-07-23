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
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding

class StrigaWithdrawFragment :
    BaseMvpFragment<StrigaWithdrawContract.View, StrigaWithdrawContract.Presenter>(
        R.layout.fragment_striga_off_ramp_withdraw
    ),
    StrigaWithdrawContract.View {

    override val presenter: StrigaWithdrawContract.Presenter by inject()
    private val binding: FragmentStrigaOffRampWithdrawBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { popBackStack() }

        binding.editTextIban.mutate()
            .setEndDrawableIsVisible(isVisible = false)
            .setDrawableStrategy(NewUiKitEditTextDrawableStrategy.SHOW_ON_TEXT)
            .setDrawableClickListener(NewUiKitEditTextMutator::clearInput)

        binding.editTextBic.mutate()
            .setEndDrawableIsVisible(isVisible = false)
            .setDrawableStrategy(NewUiKitEditTextDrawableStrategy.SHOW_ON_TEXT)
            .setDrawableClickListener(NewUiKitEditTextMutator::clearInput)
    }

    override fun showBankingDetails(offRampCredentials: StrigaUserBankingDetails) {
        offRampCredentials.bankingBic?.also { binding.editTextBic.mutate().setText(it) }
        offRampCredentials.bankingIban?.also { binding.editTextIban.mutate().setText(it) }
        binding.editTextReceiver.mutate().setText(offRampCredentials.bankingFullName)
    }

    override fun showIbanIsValid(validationResult: StrigaWithdrawValidationResult) {
        val error = validationResult.errorTextRes?.let(TextContainer::invoke)
        binding.editTextIban.mutate()
            .setError(error)
    }

    override fun showBicIsValid(validationResult: StrigaWithdrawValidationResult) {
        val error = validationResult.errorTextRes?.let(TextContainer::invoke)
        binding.editTextBic.mutate()
            .setError(error)
    }
}
