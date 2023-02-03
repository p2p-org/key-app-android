package org.p2p.wallet.renbtc.ui.main

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import org.koin.android.ext.android.inject
import org.p2p.uikit.utils.SpanUtils
import org.p2p.uikit.utils.SpanUtils.highlightPublicKey
import org.p2p.uikit.utils.toast
import org.p2p.wallet.R
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.permissions.PermissionsDialog
import org.p2p.wallet.databinding.FragmentRenBtcBinding
import org.p2p.wallet.receive.network.ReceiveNetworkTypeFragment
import org.p2p.wallet.receive.solana.ReceiveSolanaFragment
import org.p2p.wallet.receive.widget.BaseQrCodeFragment
import org.p2p.wallet.receive.widget.ReceiveCardView
import org.p2p.wallet.renbtc.ui.transactions.RenTransactionsFragment
import org.p2p.wallet.newsend.model.NetworkType
import org.p2p.core.utils.Constants
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.shareScreenShot
import org.p2p.wallet.utils.showErrorDialog
import org.p2p.wallet.utils.showUrlInCustomTabs
import org.p2p.wallet.utils.viewbinding.viewBinding
import java.io.File

class RenBTCFragment :
    BaseQrCodeFragment<RenBTCContract.View, RenBTCContract.Presenter>(R.layout.fragment_ren_btc),
    RenBTCContract.View,
    PermissionsDialog.Callback {

    companion object {
        private const val REQUEST_KEY = "REQUEST_KEY"
        private const val BUNDLE_KEY_NETWORK_TYPE = "BUNDLE_KEY_NETWORK_TYPE"
        fun create() = RenBTCFragment()
    }

    override val presenter: RenBTCContract.Presenter by inject()
    private val binding: FragmentRenBtcBinding by viewBinding()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()
    override val receiveCardView: ReceiveCardView by lazy { binding.receiveCardView }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        analyticsInteractor.logScreenOpenEvent(ScreenNames.Receive.BITCOIN)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }

            statusView.setOnClickListener {
                presenter.onStatusReceivedClicked()
            }

            receiveCardView.setOnRequestPermissions {
                checkStatusAndRequestPermissionsIfNotGranted()
            }
            receiveCardView.setOnSaveQrClickListener { name, qrImage ->
                presenter.saveQr(name, qrImage)
            }
            receiveCardView.setOnShareQrClickListener { name, qrImage, shareText ->
                presenter.saveQr(name, qrImage, shareText)
            }
            receiveCardView.setOnNetworkClickListener {
                presenter.onNetworkClicked()
            }
            receiveCardView.setSelectNetworkVisibility(isVisible = true)
            receiveCardView.setFaqVisibility(isVisible = false)
            receiveCardView.setQrWatermark(R.drawable.ic_btc)
            receiveCardView.setTokenSymbol(Constants.REN_BTC_SYMBOL)
            receiveCardView.setNetworkName(getString(R.string.send_bitcoin_network))

            setFragmentResultListener(REQUEST_KEY) { _, bundle ->
                val type = bundle.get(BUNDLE_KEY_NETWORK_TYPE) as NetworkType
                if (type == NetworkType.SOLANA) {
                    popAndReplaceFragment(ReceiveSolanaFragment.create(null))
                }
            }
        }

        presenter.subscribe()
        presenter.checkActiveSession(requireContext())
        presenter.startNewSession(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.cancelTimer()
    }

    override fun renderQr(qrBitmap: Bitmap?) {
        if (qrBitmap != null) {
            binding.receiveCardView.setQrBitmap(qrBitmap)
        }
    }

    override fun showActiveState(address: String, remaining: String, fee: String) {
        with(binding) {
            receiveCardView.setQrValue(address.highlightPublicKey(requireContext()))

            progressButton.setOnClickListener {
                presenter.onBrowserClicked(address)
            }

            val infoText = getString(R.string.receive_session_info)
            val onlyBitcoin = getString(R.string.receive_only_bitcoin)
            sessionInfoTextView.text = SpanUtils.setTextBold(infoText, onlyBitcoin)

            val btcText = getString(R.string.common_btc)
            val amountText = getString(R.string.receive_session_min_transaction, fee)
            amountInfoTextView.text = SpanUtils.setTextBold(amountText, fee, btcText)
        }
    }

    override fun updateTimer(remaining: String) {
        val text = getString(R.string.receive_session_timer_info, remaining)
        binding.timerTextView.text = SpanUtils.setTextBold(text, remaining)
        binding.timerTextView.isVisible = true
    }

    override fun showLoading(isLoading: Boolean) {
        binding.receiveCardView.showQrLoading(isLoading)
    }

    override fun showToastMessage(resId: Int) {
        toast(resId)
    }

    override fun showTransactionsCount(count: Int) {
        binding.statusCountTextView.text = count.toString()
        binding.statusView.isEnabled = count != 0
    }

    override fun navigateToSolana() {
        popAndReplaceFragment(ReceiveSolanaFragment.create(null))
    }

    override fun showNetwork() {
        replaceFragment(ReceiveNetworkTypeFragment.create(NetworkType.BITCOIN, REQUEST_KEY, BUNDLE_KEY_NETWORK_TYPE))
    }

    override fun showBrowser(url: String) {
        showUrlInCustomTabs(url)
    }

    override fun showStatuses() {
        replaceFragment(RenTransactionsFragment.create())
    }

    override fun showErrorMessage(e: Throwable?) {
        showErrorDialog(e) {
            popBackStack()
        }
    }

    override fun showShareQr(qrImage: File, qrValue: String) {
        requireContext().shareScreenShot(qrImage, qrValue)
    }
}
