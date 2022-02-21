package org.p2p.wallet.moonpay.ui

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.textwatcher.PrefixTextWatcher
import org.p2p.wallet.databinding.FragmentBuySolanaBinding
import org.p2p.wallet.moonpay.model.BuyData
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withTextOrGone
import timber.log.Timber

class BuySolanaFragment :
    BaseMvpFragment<BuySolanaContract.View, BuySolanaContract.Presenter>(R.layout.fragment_buy_solana),
    BuySolanaContract.View {

    companion object {
        fun create() = BuySolanaFragment()
    }

    override val presenter: BuySolanaContract.Presenter by inject()

    private val binding: FragmentBuySolanaBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener {
                popBackStack()
                logBuy()
            }

            PrefixTextWatcher.installOn(payEditText) { data ->
                purchaseCostView.setValueText(data.prefixText)
                continueButton.isEnabled = data.prefixText.isNotEmpty() && !hasInputError()
                presenter.setBuyAmount(data.valueWithoutPrefix)
            }

            continueButton.setOnClickListener {
                presenter.onContinueClicked()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            logBuy()
            popBackStack()
        }

        presenter.loadData()
    }

    override fun showTokenPrice(price: String) {
        binding.priceView.setValueText(price)
    }

    override fun showData(data: BuyData) {
        with(binding) {
            priceView.setValueText(data.priceText)
            getValueTextView.text = data.receiveAmountText
            processingFeeView.setValueText(data.processingFeeText)
            networkFeeView.setValueText(data.networkFeeText)
            extraFeeView.setValueText(data.extraFeeText)
            accountCreationView.setValueText(data.accountCreationCostText)

            totalView.setValueText(data.totalText)
        }
    }

    override fun showLoading(isLoading: Boolean) {
        binding.progressBar.isInvisible = !isLoading
    }

    override fun showMessage(message: String?) {
        binding.apply {
            errorTextView withTextOrGone message
            continueButton.isEnabled = !hasInputError()
        }
    }

    override fun navigateToMoonpay(amount: String) {
        replaceFragment(MoonpayViewFragment.create(amount))
    }

    private fun logBuy() {
        Timber.tag("###").d("something logged")
    }

    private fun hasInputError(): Boolean = binding.errorTextView.isVisible
}