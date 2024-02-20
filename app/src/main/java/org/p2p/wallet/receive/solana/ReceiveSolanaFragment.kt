package org.p2p.wallet.receive.solana

import androidx.core.view.isVisible
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import java.io.File
import org.p2p.core.analytics.constants.ScreenNames
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants
import org.p2p.uikit.utils.SpanUtils.highlightPublicKey
import org.p2p.uikit.utils.toast
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.databinding.FragmentReceiveSolanaBinding
import org.p2p.wallet.receive.analytics.ReceiveAnalytics
import org.p2p.wallet.receive.list.ReceiveTokenListFragment
import org.p2p.wallet.receive.widget.BaseQrCodeFragment
import org.p2p.wallet.receive.widget.ReceiveCardView
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.shareScreenShot
import org.p2p.wallet.utils.showUrlInCustomTabs
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

class ReceiveSolanaFragment :
    BaseQrCodeFragment<ReceiveSolanaContract.View, ReceiveSolanaContract.Presenter>(R.layout.fragment_receive_solana),
    ReceiveSolanaContract.View {

    companion object {
        private const val ARG_TOKEN = "EXTRA_TOKEN"

        fun create(token: Token?): ReceiveSolanaFragment =
            ReceiveSolanaFragment()
                .withArgs(ARG_TOKEN to token)
    }

    private val token: Token? by args(ARG_TOKEN)

    override val presenter: ReceiveSolanaContract.Presenter by inject { parametersOf(token) }

    override val receiveCardView: ReceiveCardView
        get() = binding.receiveCardView

    private val binding: FragmentReceiveSolanaBinding by viewBinding()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()
    private val receiveAnalytics: ReceiveAnalytics by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { popBackStack() }

        with(receiveCardView) {
            setOnFaqClickListener {
                analyticsInteractor.logScreenOpenEvent(ScreenNames.Receive.LIST)
                replaceFragment(ReceiveTokenListFragment.create())
            }
            setOnRequestPermissions(::checkStatusAndRequestPermissionsIfNotGranted)
            setOnShareQrClickListener { qrValue, qrImage, shareText ->
                presenter.saveQr(qrValue, qrImage, shareText)
                receiveAnalytics.logUserCardShared(analyticsInteractor.getPreviousScreenName())
            }
            setOnCopyQrClickListener {
                receiveAnalytics.logReceiveAddressCopied(analyticsInteractor.getPreviousScreenName())
            }
            setOnSaveQrClickListener(presenter::saveQr)
            receiveCardView.setTokenSymbol(token?.tokenSymbol ?: Constants.SOL_SYMBOL)
            receiveCardView.setSelectNetworkVisibility(isVisible = true)
            receiveCardView.setChevronInvisible(isInvisible = true)
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
