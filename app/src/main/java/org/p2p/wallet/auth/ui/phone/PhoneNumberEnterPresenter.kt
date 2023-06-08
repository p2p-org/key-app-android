package org.p2p.wallet.auth.ui.phone

import timber.log.Timber
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.auth.analytics.CreateWalletAnalytics
import org.p2p.wallet.auth.analytics.RestoreWalletAnalytics
import org.p2p.wallet.auth.gateway.repository.model.PushServiceError
import org.p2p.wallet.auth.interactor.CreateWalletInteractor
import org.p2p.wallet.auth.interactor.OnboardingInteractor
import org.p2p.wallet.auth.interactor.restore.RestoreWalletInteractor
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.auth.model.GatewayHandledState
import org.p2p.wallet.auth.model.OnboardingFlow
import org.p2p.wallet.auth.model.PhoneNumber
import org.p2p.wallet.auth.repository.CountryCodeRepository
import org.p2p.wallet.auth.repository.GatewayServiceErrorHandler
import org.p2p.wallet.common.mvp.BasePresenter

private const val MAX_PHONE_NUMBER_TRIES = 5
private const val DEFAULT_BLOCK_TIME_IN_MINUTES = 10

class PhoneNumberEnterPresenter(
    private val countryCodeRepository: CountryCodeRepository,
    private val createWalletInteractor: CreateWalletInteractor,
    private val restoreWalletInteractor: RestoreWalletInteractor,
    private val onboardingInteractor: OnboardingInteractor,
    private val createWalletAnalytics: CreateWalletAnalytics,
    private val restoreWalletAnalytics: RestoreWalletAnalytics,
    private val gatewayServiceErrorHandler: GatewayServiceErrorHandler,
) : BasePresenter<PhoneNumberEnterContract.View>(), PhoneNumberEnterContract.Presenter {

    private var selectedCountryCode: CountryCode? = null

    override fun attach(view: PhoneNumberEnterContract.View) {
        super.attach(view)

        when (onboardingInteractor.currentFlow) {
            is OnboardingFlow.CreateWallet -> view.initCreateWalletViews()
            is OnboardingFlow.RestoreWallet -> view.initRestoreWalletViews()
        }

        selectedCountryCode?.let { countryCode ->
            view.showDefaultCountryCode(countryCode)
        } ?: launch { loadDefaultCountryCode() }
    }

    private suspend fun loadDefaultCountryCode() {
        try {
            val countryCode: CountryCode = countryCodeRepository.detectCountryOrDefault()
            selectedCountryCode = countryCode

            view?.showDefaultCountryCode(countryCode)
        } catch (e: Throwable) {
            Timber.e(e, "Loading default country code failed")
            view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
        }
    }

    override fun onCountryCodeChanged(newCountryCode: String) {
        launch {
            selectedCountryCode = countryCodeRepository.findCountryCodeByPhoneCode(newCountryCode)
            view?.update(selectedCountryCode)
        }
    }

    override fun onPhoneChanged(phoneNumber: String) {
        selectedCountryCode?.let {
            val isValidNumber = countryCodeRepository.isValidNumberForRegion(
                regionCode = it.phoneCode,
                phoneNumber = phoneNumber
            )
            val newButtonState = if (isValidNumber) {
                PhoneNumberScreenContinueButtonState.ENABLED_TO_CONTINUE
            } else {
                PhoneNumberScreenContinueButtonState.DISABLED_INPUT_IS_EMPTY
            }
            view?.setContinueButtonState(newButtonState)
        }
    }

    override fun onCountryCodeChanged(newCountry: CountryCode?) {
        selectedCountryCode = newCountry ?: selectedCountryCode
        view?.update(selectedCountryCode)
    }

    override fun onCountryCodeInputClicked() {
        view?.showCountryCodePicker(selectedCountryCode)
    }

    override fun submitUserPhoneNumber(phoneNumber: String) {
        launch {
            view?.setLoadingState(isLoading = true)
            val userPhoneNumber = PhoneNumber(selectedCountryCode?.phoneCode + phoneNumber)
            onboardingInteractor.temporaryPhoneNumber = userPhoneNumber
            when (onboardingInteractor.currentFlow) {
                is OnboardingFlow.CreateWallet -> {
                    createWalletAnalytics.logCreateConfirmPhoneButtonClicked()
                    startCreatingWallet(userPhoneNumber)
                }
                is OnboardingFlow.RestoreWallet -> {
                    restoreWalletAnalytics.logRestoreConfirmPhoneButtonClicked()
                    startRestoringCustomShare(userPhoneNumber)
                }
            }
            view?.setLoadingState(isLoading = false)
        }
    }

    private suspend fun startCreatingWallet(phoneNumber: PhoneNumber) {
        try {
            selectedCountryCode?.let {
                if (createWalletInteractor.getUserEnterPhoneNumberTriesCount() >= MAX_PHONE_NUMBER_TRIES) {
                    createWalletInteractor.resetUserEnterPhoneNumberTriesCount()
                    view?.navigateToAccountBlocked(DEFAULT_BLOCK_TIME_IN_MINUTES.minutes.inWholeSeconds)
                } else {
                    createWalletInteractor.startCreatingWallet(userPhoneNumber = phoneNumber)
                    view?.navigateToSmsInput()
                }
            }
        } catch (error: Throwable) {
            if (error is PushServiceError) {
                handleGatewayServiceError(error)
            } else {
                Timber.e(error, "Phone number submission failed")
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            }
            createWalletInteractor.setIsCreateWalletRequestSent(isSent = false)
        }
    }

    private fun handleGatewayServiceError(gatewayServiceError: PushServiceError) {
        when (val gatewayHandledResult = gatewayServiceErrorHandler.handle(gatewayServiceError)) {
            is GatewayHandledState.CriticalError -> {
                view?.navigateToCriticalErrorScreen(gatewayHandledResult)
            }
            is GatewayHandledState.TimerBlockError -> {
                view?.navigateToAccountBlocked(gatewayHandledResult.cooldownTtl)
            }
            is GatewayHandledState.TitleSubtitleError -> {
                view?.navigateToCriticalErrorScreen(gatewayHandledResult)
            }
            is GatewayHandledState.ToastError -> {
                view?.showUiKitSnackBar(gatewayHandledResult.message)
            }
            else -> {
                // Do nothing
            }
        }
    }

    private suspend fun startRestoringCustomShare(phoneNumber: PhoneNumber) {
        try {
            selectedCountryCode?.let {
                if (restoreWalletInteractor.getUserEnterPhoneNumberTriesCount() >= MAX_PHONE_NUMBER_TRIES) {
                    restoreWalletInteractor.resetUserEnterPhoneNumberTriesCount()
                    view?.navigateToAccountBlocked(DEFAULT_BLOCK_TIME_IN_MINUTES.minutes.inWholeSeconds)
                } else {
                    restoreWalletInteractor.startRestoreCustomShare(userPhoneNumber = phoneNumber)
                    view?.navigateToSmsInput()
                }
            }
        } catch (error: Throwable) {
            if (error is PushServiceError) {
                handleGatewayServiceError(error)
            } else {
                Timber.e(error, "Phone number submission failed")
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            }
            restoreWalletInteractor.setIsRestoreWalletRequestSent(isSent = false)
        }
    }
}
