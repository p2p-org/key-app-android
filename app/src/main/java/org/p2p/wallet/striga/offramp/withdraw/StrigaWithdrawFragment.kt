package org.p2p.wallet.striga.offramp.withdraw

import android.content.Context
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.core.common.TextContainer
import org.p2p.uikit.components.edittext.v2.NewUiKitEditTextDrawableStrategy
import org.p2p.uikit.components.edittext.v2.NewUiKitEditTextMutator
import org.p2p.uikit.utils.SimpleMaskFormatter
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentStrigaOffRampWithdrawBinding
import org.p2p.wallet.home.ui.container.MainContainerFragment
import org.p2p.wallet.root.RootListener
import org.p2p.wallet.striga.StrigaFragmentFactory
import org.p2p.wallet.striga.wallet.models.StrigaUserBankingDetails
import org.p2p.wallet.striga.wallet.models.ids.StrigaWithdrawalChallengeId
import org.p2p.wallet.transaction.model.NewShowProgress
import org.p2p.wallet.transaction.progresshandler.StrigaOffRampTransactionProgressHandler
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.popBackStackTo
import org.p2p.wallet.utils.replaceFragment
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
    private val strigaFragmentFactory: StrigaFragmentFactory by inject()
    private var listener: RootListener? = null

    private var isButtonOnceClicked: Boolean = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? RootListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { popBackStack() }

        val ibanMaskFormatter = SimpleMaskFormatter(mask = "#### #### ##### ##### #### #### ##")

        binding.editTextIban.mutate {
            setEndDrawableIsVisible(isVisible = false)
            setDrawableStrategy(NewUiKitEditTextDrawableStrategy.SHOW_ON_TEXT)
            setDrawableClickListener(NewUiKitEditTextMutator::clearInput)
            setMaskFormatter(ibanMaskFormatter)
            addOnTextChangedListener { presenter.onIbanChanged(it.toString()) }
        }

        binding.editTextBic.mutate {
            setEndDrawableIsVisible(isVisible = false)
            setDrawableStrategy(NewUiKitEditTextDrawableStrategy.SHOW_ON_TEXT)
            setDrawableClickListener(NewUiKitEditTextMutator::clearInput)
            addOnTextChangedListener { presenter.onBicChanged(it.toString()) }
        }

        binding.buttonWithdraw.setOnClickListener {
            isButtonOnceClicked = true
            presenter.withdraw(withdrawType)
        }
    }

    override fun showLoading(isLoading: Boolean) {
        binding.buttonWithdraw.setLoading(isLoading)
    }

    override fun showPrefilledBankingDetails(details: StrigaUserBankingDetails) {
        details.bankingBic?.also { binding.editTextBic.mutate().setInputText(it) }
        details.bankingIban?.also { binding.editTextIban.mutate().setInputText(it) }
        binding.editTextReceiver.mutate().setInputText(details.bankingFullName)
    }

    override fun showIbanValidationResult(result: StrigaWithdrawValidationResult) {
        if (isButtonOnceClicked) {
            val error = result.errorTextRes?.let(TextContainer::invoke)
            binding.editTextIban.mutate().setErrorState(error)
            binding.buttonWithdraw.isEnabled = binding.editTextBic.values.isInErrorState && error != null
        }
    }

    override fun showBicValidationResult(result: StrigaWithdrawValidationResult) {
        if (isButtonOnceClicked) {
            val error = result.errorTextRes?.let(TextContainer::invoke)
            binding.editTextBic.mutate().setErrorState(error)
            binding.buttonWithdraw.isEnabled = !binding.editTextIban.values.isInErrorState && error != null
        }
    }

    override fun navigateToTransactionDetails(
        transactionId: String,
        data: NewShowProgress
    ) {
        listener?.showTransactionProgress(
            internalTransactionId = transactionId,
            data = data,
            handlerQualifierName = StrigaOffRampTransactionProgressHandler.QUALIFIER
        )
        popBackStackTo(target = MainContainerFragment::class, inclusive = false)
    }

    override fun navigateToOtpConfirm(
        challengeId: StrigaWithdrawalChallengeId
    ) {
        val fragment = strigaFragmentFactory.onRampConfirmOtpFragment(
            challengeId = challengeId
        )
        replaceFragment(fragment)
    }
}
