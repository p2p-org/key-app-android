package org.p2p.wallet.smsinput.updatedevice

import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.auth.gateway.repository.model.PushServiceError
import org.p2p.wallet.auth.interactor.CreateWalletInteractor
import org.p2p.wallet.auth.interactor.restore.RestoreWalletInteractor
import org.p2p.wallet.auth.model.GatewayHandledState
import org.p2p.wallet.auth.repository.GatewayServiceErrorHandler
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.smsinput.SmsInputContract
import org.p2p.wallet.smsinput.SmsInputContract.Presenter.SmsInputTimerState
import org.p2p.wallet.utils.removeWhiteSpaces

class UpdateDeviceSmsInputPresenter(
    private val createWalletInteractor: CreateWalletInteractor,
    private val restoreWalletInteractor: RestoreWalletInteractor,
    private val gatewayServiceErrorHandler: GatewayServiceErrorHandler,
) : BasePresenter<SmsInputContract.View>(), SmsInputContract.Presenter {

    override fun attach(view: SmsInputContract.View) {
        super.attach(view)
        view.renderSmsTimerState(SmsInputTimerState.ResendSmsReady)
        val userPhoneNumber = restoreWalletInteractor.getUserPhoneNumber()
        userPhoneNumber?.let { view.initView(it) }
        connectToTimer()
    }

    // Start count down timer
    private fun connectToTimer() {
        createWalletInteractor.timer.let { timer ->
            launch {
                timer.collect { secondsBeforeResend ->
                    view?.renderSmsTimerState(SmsInputTimerState.ResendSmsNotReady(secondsBeforeResend))
                    if (secondsBeforeResend == 0) {
                        view?.renderSmsTimerState(SmsInputTimerState.ResendSmsReady)
                    }
                }
            }
        }
    }

    // Launch when sms code has been changed
    override fun onSmsInputChanged(smsCode: String) {
        if (isSmsCodeFormatValid(smsCode)) {
            view?.renderSmsFormatValid()
        } else {
            view?.renderSmsFormatInvalid()
        }
    }

    // Check for sms code, if is valid - start to CREATE OR RESTORE wallet
    override fun checkSmsValue(smsCode: String) {
        if (smsCode.isBlank()) {
            return
        }
        launch {
            val smsCodeRaw = smsCode.removeWhiteSpaces()
            finishRestoringCustomShare(smsCodeRaw)
        }
    }

    override fun resendSms() {
        tryToResendSms()
        connectToTimer()
    }

    private fun handleGatewayError(error: PushServiceError) {
        when (val gatewayHandledResult = gatewayServiceErrorHandler.handle(error)) {
            is GatewayHandledState.CriticalError -> {
                view?.navigateToGatewayErrorScreen(gatewayHandledResult)
            }
            GatewayHandledState.IncorrectOtpCodeError -> {
                view?.renderIncorrectSms()
            }
            is GatewayHandledState.TimerBlockError -> {
                view?.navigateToSmsInputBlocked(gatewayHandledResult.cooldownTtl)
                restoreWalletInteractor.resetUserPhoneNumber()
            }
            is GatewayHandledState.TitleSubtitleError -> {
                view?.navigateToGatewayErrorScreen(gatewayHandledResult)
            }
            is GatewayHandledState.ToastError -> {
                view?.navigateToGatewayErrorScreen(gatewayHandledResult)
            }
            else -> {
                Timber.i("GatewayService error is not handled")
            }
        }
    }

    private suspend fun finishRestoringCustomShare(smsCode: String) {
        try {
            view?.renderButtonLoading(isLoading = true)
            restoreWalletInteractor.finishRestoreCustomShare(smsCode)

            view?.navigateNext()
        } catch (gatewayError: PushServiceError) {
            handleGatewayError(gatewayError)
        } catch (error: Throwable) {
            Timber.e(error, "Restoring user or custom share failed")
            view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
        } finally {
            view?.renderButtonLoading(isLoading = false)
        }
    }

    private fun tryToResendSms() {
        launch {
            try {
                val userPhoneNumber = restoreWalletInteractor.getUserPhoneNumber()
                    ?: error("User phone number cannot be null")
                restoreWalletInteractor.startRestoreCustomShare(
                    userPhoneNumber = userPhoneNumber,
                    isResend = true
                )
            } catch (gatewayError: PushServiceError) {
                handleGatewayError(gatewayError)
            } catch (error: Throwable) {
                Timber.e(error, "Resending sms failed")
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            }
        }
    }

    private fun isSmsCodeFormatValid(smsCode: String): Boolean {
        return smsCode.length == 6
    }
}
