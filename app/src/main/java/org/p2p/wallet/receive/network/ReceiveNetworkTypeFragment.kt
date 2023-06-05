package org.p2p.wallet.receive.network

import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.wallet.R
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentReceiveNetworkTypeBinding
import org.p2p.core.token.Token
import org.p2p.wallet.home.ui.select.bottomsheet.SelectTokenBottomSheet
import org.p2p.wallet.moonpay.ui.BuySolanaFragment
import org.p2p.wallet.moonpay.ui.new.NewBuyFragment
import org.p2p.wallet.renbtc.ui.info.RenBtcBuyBottomSheet
import org.p2p.wallet.renbtc.ui.info.RenBtcCreateByFeeRelayBottomSheet
import org.p2p.wallet.renbtc.ui.info.RenBtcInfoBottomSheet
import org.p2p.wallet.renbtc.ui.info.RenBtcTopupBottomSheet
import org.p2p.wallet.send.model.NetworkType
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import java.math.BigDecimal
import org.p2p.wallet.utils.getParcelableCompat

private const val EXTRA_NETWORK_TYPE = "EXTRA_NETWORK_TYPE"
private const val EXTRA_REQUEST_KEY = "EXTRA_REQUEST_KEY"
private const val EXTRA_RESULT_KEY = "EXTRA_RESULT_KEY"

class ReceiveNetworkTypeFragment :
    BaseMvpFragment<ReceiveNetworkTypeContract.View, ReceiveNetworkTypeContract.Presenter>(
        R.layout.fragment_receive_network_type
    ),
    ReceiveNetworkTypeContract.View {

    companion object {
        private const val REQUEST_KEY = "REQUEST_KEY"
        private const val BUNDLE_KEY_IS_TOPUP_SELECTED = "BUNDLE_KEY_IS_TOPUP_SELECTED"
        private const val BUNDLE_KEY_IS_BUY_SELECTED = "BUNDLE_KEY_IS_BUY_SELECTED"
        private const val BUNDLE_KEY_IS_BTC_SELECTED = "BUNDLE_KEY_IS_BTC_SELECTED"
        private const val BUNDLE_KEY_SELECTED_TOKEN = "BUNDLE_KEY_SELECTED_TOKEN"
        fun create(
            networkType: NetworkType = NetworkType.SOLANA,
            requestKey: String,
            resultKey: String
        ) = ReceiveNetworkTypeFragment().withArgs(
            EXTRA_NETWORK_TYPE to networkType,
            EXTRA_REQUEST_KEY to requestKey,
            EXTRA_RESULT_KEY to resultKey
        )
    }

    override val presenter: ReceiveNetworkTypeContract.Presenter by inject {
        parametersOf(networkType)
    }
    private val binding: FragmentReceiveNetworkTypeBinding by viewBinding()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()

    private val networkType: NetworkType by args(EXTRA_NETWORK_TYPE)
    private val requestKey: String by args(EXTRA_REQUEST_KEY)
    private val resultKey: String by args(EXTRA_RESULT_KEY)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        analyticsInteractor.logScreenOpenEvent(ScreenNames.Receive.NETWORK)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            solanaButton.setOnClickListener {
                presenter.onNetworkChanged(NetworkType.SOLANA)
            }
            btcButton.setOnClickListener {
                presenter.onNetworkChanged(NetworkType.BITCOIN)
            }
        }
        childFragmentManager.setFragmentResultListener(REQUEST_KEY, viewLifecycleOwner) { _, bundle ->
            if (bundle.containsKey(BUNDLE_KEY_IS_TOPUP_SELECTED)) {
                onRenBtcTopUpResult(bundle)
            }
            if (bundle.containsKey(BUNDLE_KEY_IS_BUY_SELECTED)) {
                onBuyResult(bundle)
            }
            if (bundle.containsKey(BUNDLE_KEY_IS_BTC_SELECTED)) {
                onBtcInfoResult(bundle)
            }

            if (bundle.containsKey(BUNDLE_KEY_SELECTED_TOKEN)) {
                val token = bundle.getParcelableCompat<Token>(BUNDLE_KEY_SELECTED_TOKEN)
                if (token != null) {
                    popAndReplaceFragment(
                        BuySolanaFragment.create(token)
                    )
                }
            }
        }
        presenter.load()
    }

    override fun showNewBuyFragment(token: Token) {
        popAndReplaceFragment(NewBuyFragment.create(token))
    }

    override fun showNetworkInfo(type: NetworkType) {
        RenBtcInfoBottomSheet.show(childFragmentManager, REQUEST_KEY, BUNDLE_KEY_IS_BTC_SELECTED)
        analyticsInteractor.logScreenOpenEvent(ScreenNames.Receive.BITCOIN_INFO)
    }

    override fun setCheckState(type: NetworkType) {
        with(binding) {
            solanaRadioButton.isSelected = type == NetworkType.SOLANA
            btcRadioButton.isSelected = type == NetworkType.BITCOIN
        }
    }

    override fun navigateToReceive(type: NetworkType) {
        setFragmentResult(requestKey = requestKey, result = bundleOf(resultKey to type))
        popBackStack()
    }

    override fun showLoading(isLoading: Boolean) {
        binding.progressView.isVisible = isLoading
    }

    override fun showBuy(priceInSol: BigDecimal, priceInUsd: BigDecimal?) {
        RenBtcBuyBottomSheet.show(
            fm = childFragmentManager,
            priceInSol = priceInSol,
            priceInUsd = priceInUsd,
            requestKey = REQUEST_KEY,
            resultKey = BUNDLE_KEY_IS_BUY_SELECTED
        )
    }

    override fun showCreateByFeeRelay() {
        RenBtcCreateByFeeRelayBottomSheet.show(
            fm = childFragmentManager,
            requestKey = REQUEST_KEY,
            resultKey = BUNDLE_KEY_IS_BUY_SELECTED
        )
    }

    override fun showTokensForBuy(tokens: List<Token>) {
        SelectTokenBottomSheet.show(childFragmentManager, tokens, REQUEST_KEY, BUNDLE_KEY_SELECTED_TOKEN)
    }

    override fun showTopup() {
        RenBtcTopupBottomSheet.show(childFragmentManager, REQUEST_KEY, BUNDLE_KEY_IS_TOPUP_SELECTED)
    }

    override fun close() {
        popBackStack()
    }

    private fun onRenBtcTopUpResult(bundle: Bundle) {
        val isTopUpSelected = bundle.getBoolean(BUNDLE_KEY_IS_TOPUP_SELECTED)
        presenter.onTopupSelected(isTopUpSelected)
    }

    private fun onBuyResult(bundle: Bundle) {
        val isBuySelected = bundle.getBoolean(BUNDLE_KEY_IS_BUY_SELECTED)
        presenter.onBuySelected(isBuySelected)
    }

    private fun onBtcInfoResult(bundle: Bundle) {
        val isBtcSelected = bundle.getBoolean(BUNDLE_KEY_IS_BTC_SELECTED)
        presenter.onBtcSelected(isBtcSelected)
    }
}
