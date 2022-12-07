package org.p2p.wallet.receive.solana

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.uikit.utils.SpanUtils.highlightPublicKey
import org.p2p.uikit.utils.toast
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.databinding.FragmentReceiveSolanaBinding
import org.p2p.core.token.Token
import org.p2p.wallet.receive.analytics.ReceiveAnalytics
import org.p2p.wallet.receive.list.TokenListFragment
import org.p2p.wallet.receive.network.ReceiveNetworkTypeFragment
import org.p2p.wallet.receive.widget.BaseQrCodeFragment
import org.p2p.wallet.receive.widget.ReceiveCardView
import org.p2p.wallet.renbtc.ui.main.RenBTCFragment
import org.p2p.wallet.send.model.NetworkType
import org.p2p.core.utils.Constants
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.shareScreenShot
import org.p2p.wallet.utils.showUrlInCustomTabs
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import java.io.File

class ReceiveSolanaFragment :
    BaseQrCodeFragment<ReceiveSolanaContract.View, ReceiveSolanaContract.Presenter>(R.layout.fragment_receive_solana),
    ReceiveSolanaContract.View {

    companion object {
        private const val EXTRA_TOKEN = "EXTRA_TOKEN"
        private const val REQUEST_KEY = "REQUEST_KEY"
        private const val BUNDLE_KEY_NETWORK_TYPE = "BUNDLE_KEY_NETWORK_TYPE"
        fun create(token: Token?) = ReceiveSolanaFragment().withArgs(
            EXTRA_TOKEN to token
        )
    }

    private val token: Token? by args(EXTRA_TOKEN)
    override val presenter: ReceiveSolanaContract.Presenter by inject {
        parametersOf(token)
    }
    override val receiveCardView: ReceiveCardView by lazy { binding.receiveCardView }
    private val binding: FragmentReceiveSolanaBinding by viewBinding()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()
    private val receiveAnalytics: ReceiveAnalytics by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            receiveCardView.setOnFaqClickListener {
                analyticsInteractor.logScreenOpenEvent(ScreenNames.Receive.LIST)
                replaceFragment(TokenListFragment.create())
            }
            receiveCardView.setOnRequestPermissions {
                checkStatusAndRequestPermissionsIfNotGranted()
            }
            receiveCardView.setOnShareQrClickListener { qrValue, qrImage, shareText ->
                presenter.saveQr(qrValue, qrImage, shareText)
                receiveAnalytics.logUserCardShared(analyticsInteractor.getPreviousScreenName())
            }
            receiveCardView.setOnCopyQrClickListener {
                receiveAnalytics.logReceiveAddressCopied(analyticsInteractor.getPreviousScreenName())
            }
            receiveCardView.setOnSaveQrClickListener { qrValue, qrImage ->
                presenter.saveQr(qrValue, qrImage)
            }
            receiveCardView.setTokenSymbol(token?.tokenSymbol ?: Constants.SOL_SYMBOL)
            receiveCardView.setSelectNetworkVisibility(isVisible = true)
            receiveCardView.setChevronInvisible(isInvisible = true)
        }

        setFragmentResultListener(REQUEST_KEY) { _, bundle ->
            val type = bundle.get(BUNDLE_KEY_NETWORK_TYPE) as NetworkType
            if (type == NetworkType.BITCOIN) {
                popAndReplaceFragment(RenBTCFragment.create())
            }
        }
        presenter.loadData()

        receiveAnalytics.logStartScreen(analyticsInteractor.getPreviousScreenName())
    }

    override fun showUserData(userPublicKey: String, username: Username?) {
        with(binding) {
            receiveCardView.setQrValue(userPublicKey.highlightPublicKey(requireContext()))
            username?.fullUsername?.let(receiveCardView::setQrName)
            progressButton.setOnClickListener {
                val url = getString(R.string.solanaWalletExplorer, userPublicKey)
                showUrlInCustomTabs(url)
            }
        }
    }

    override fun showReceiveToken(token: Token.Active) {
        binding.progressButton.setOnClickListener {
            presenter.onBrowserClicked(token.publicKey)
            val url = getString(R.string.solanaWalletExplorer, token.publicKey)
            showUrlInCustomTabs(url)
        }
    }

    override fun renderQr(qrBitmap: Bitmap?) {
        if (qrBitmap != null) {
            binding.receiveCardView.setQrBitmap(qrBitmap)
        }
    }

    override fun showQrLoading(isLoading: Boolean) {
        binding.receiveCardView.showQrLoading(isLoading)
        binding.progressView.isVisible = isLoading
    }

    override fun showToastMessage(resId: Int) {
        toast(resId)
    }

    override fun showNetwork() {
        replaceFragment(ReceiveNetworkTypeFragment.create(NetworkType.SOLANA, REQUEST_KEY, BUNDLE_KEY_NETWORK_TYPE))
    }

    override fun showBrowser(url: String) {
        showUrlInCustomTabs(url)
    }

    override fun showShareQr(qrImage: File, qrValue: String) {
        requireContext().shareScreenShot(qrImage, qrValue)
    }

    override fun showFullScreenLoading(isLoading: Boolean) {
        binding.progressView.isVisible = isLoading
    }
}
