package org.p2p.wallet.auth.ui.username

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import java.io.File
import org.p2p.core.utils.Constants
import org.p2p.uikit.utils.SpanUtils.highlightPublicKey
import org.p2p.uikit.utils.toast
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.databinding.FragmentUsernameBinding
import org.p2p.wallet.receive.analytics.ReceiveAnalytics
import org.p2p.wallet.receive.list.ReceiveTokenListFragment
import org.p2p.wallet.receive.widget.BaseQrCodeFragment
import org.p2p.wallet.receive.widget.ReceiveCardView
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.shareScreenShot
import org.p2p.wallet.utils.viewbinding.viewBinding

class UsernameFragment :
    BaseQrCodeFragment<UsernameContract.View, UsernameContract.Presenter>(R.layout.fragment_username),
    UsernameContract.View {

    companion object {
        fun create() = UsernameFragment()
    }

    override val presenter: UsernameContract.Presenter by inject()

    private val binding: FragmentUsernameBinding by viewBinding()
    private val receiveAnalytics: ReceiveAnalytics by inject()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()
    override val receiveCardView: ReceiveCardView by lazy { binding.receiveCardView }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            toolbar.setNavigationOnClickListener { popBackStack() }
            receiveCardView.initReceiveCardView()
            progressButton.setOnClickListener {
                replaceFragment(ReceiveTokenListFragment.create())
            }
        }
        presenter.loadData()
    }

    private fun ReceiveCardView.initReceiveCardView() {
        setOnRequestPermissions {
            checkStatusAndRequestPermissionsIfNotGranted()
        }
        setOnShareQrClickListener { qrValue, qrImage, shareText ->
            presenter.saveQr(qrValue, qrImage, shareText)
            receiveAnalytics.logUserCardShared(analyticsInteractor.getPreviousScreenName())
        }
        setOnSaveQrClickListener { qrValue, qrImage ->
            presenter.saveQr(qrValue, qrImage)
            receiveAnalytics.logReceiveQrSaved(analyticsInteractor.getPreviousScreenName())
        }
        setSelectNetworkVisibility(isVisible = false)
        setFaqVisibility(isVisible = false)
        setTokenSymbol(Constants.SOL_NAME)
        hideWatermark()
    }

    override fun showUsername(username: Username) {
        binding.receiveCardView.setQrName(username.fullUsername)

        binding.receiveCardView.setOnCopyQrClickListener {
            receiveAnalytics.logReceiveAddressCopied(analyticsInteractor.getPreviousScreenName())
        }

        binding.receiveCardView.setOnShareQrClickListener { qrValue, qrImage, shareText ->
            presenter.saveQr(qrValue, qrImage, shareText)
            receiveAnalytics.logUserCardShared(analyticsInteractor.getPreviousScreenName())
        }
    }

    override fun renderQr(qrBitmap: Bitmap) {
        binding.receiveCardView.setQrBitmap(qrBitmap)
        binding.receiveCardView.showQrLoading(false)
    }

    override fun showAddress(address: String) {
        binding.receiveCardView.setQrValue(address.highlightPublicKey(requireContext()))
    }

    override fun showToastMessage(messageRes: Int) {
        toast(messageRes)
    }

    override fun showShareQr(qrImage: File, qrValue: String) {
        requireContext().shareScreenShot(qrImage, qrValue)
    }
}
