package com.p2p.wallet.main.ui.receive

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.qr.model.BarcodeEncoder
import com.p2p.wallet.qr.model.QrColors
import com.p2p.wallet.token.model.Token
import com.p2p.wallet.user.interactor.UserInteractor
import com.p2p.wallet.utils.dip
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.CancellationException
import kotlin.properties.Delegates

private const val QR_BITMAP_SIZE = 600
private const val QR_LOGO_SIZE = 44
private const val LOGO_CORNERS = 12

class ReceivePresenter(
    private val defaultToken: Token?,
    private val userInteractor: UserInteractor,
    private val qrColors: QrColors
) : BasePresenter<ReceiveContract.View>(), ReceiveContract.Presenter {

    private var qrJob: Job? = null

    private var token: Token? by Delegates.observable(null) { _, _, newValue ->
        if (newValue != null) view?.showReceiveToken(newValue)
    }

    override fun setReceiveToken(context: Context, newToken: Token) {
        token = newToken

        generateQrCode(context, newToken.publicKey)
    }

    override fun loadData(context: Context) {
        launch {
            view?.showFullScreenLoading(true)
            val tokens = userInteractor.getTokens()
            val receive = defaultToken ?: tokens.firstOrNull() ?: return@launch
            token = receive

            generateQrCode(context, receive.publicKey)

            view?.showFullScreenLoading(false)
        }
    }

    override fun loadTokensForSelection() {
        launch {
            val tokens = userInteractor.getTokens()
            view?.navigateToTokenSelection(tokens)
        }
    }

    private fun generateQrCode(context: Context, address: String) {
        qrJob?.cancel()
        qrJob = launch {
            try {
                view?.showQrLoading(true)
                generateQR(address, context)
            } catch (e: CancellationException) {
                Timber.d("Qr generation was cancelled")
            } catch (e: Throwable) {
                Timber.e(e, "Failed to generate qr bitmap")
                view?.showErrorMessage()
            } finally {
                view?.showQrLoading(false)
            }
        }
    }

    private fun generateQR(address: String, context: Context) {
        val override = RequestOptions()
            .override(context.dip(QR_LOGO_SIZE))
            .transform(RoundedCorners(context.dip(LOGO_CORNERS)))
        Glide
            .with(context)
            .asBitmap()
            .apply(override)
            .load(token!!.iconUrl)
            .into(
                object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        val result = BarcodeEncoder.createQRcode(
                            resource,
                            address,
                            QR_BITMAP_SIZE,
                            QR_BITMAP_SIZE,
                            qrColors
                        )
                        view?.renderQr(result)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        view?.showErrorMessage()
                    }
                }
            )
    }
}