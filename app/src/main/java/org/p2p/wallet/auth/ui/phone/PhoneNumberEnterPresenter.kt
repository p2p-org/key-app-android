package org.p2p.wallet.auth.ui.phone

import org.p2p.wallet.R
import org.p2p.wallet.auth.gateway.repository.GatewayServiceError
import org.p2p.wallet.auth.interactor.CreateWalletInteractor
import org.p2p.wallet.auth.interactor.OnboardingInteractor
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.common.mvp.BasePresenter
import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.wallet.auth.interactor.restore.RestoreWalletInteractor
import org.p2p.wallet.auth.model.OnboardingFlow
import org.p2p.wallet.auth.model.PhoneNumber
import org.p2p.wallet.auth.ui.generalerror.GeneralErrorScreenError
import org.p2p.wallet.common.ResourcesProvider

private const val MAX_PHONE_NUMBER_TRIES = 80

class PhoneNumberEnterPresenter(
    private val countryCodeInteractor: CountryCodeInteractor,
    private val createWalletInteractor: CreateWalletInteractor,
    private val restoreWalletInteractor: RestoreWalletInteractor,
    private val onboardingInteractor: OnboardingInteractor,
    private val resourcesProvider: ResourcesProvider
) : BasePresenter<PhoneNumberEnterContract.View>(), PhoneNumberEnterContract.Presenter {

    private var selectedCountryCode: CountryCode? = null

    override fun attach(view: PhoneNumberEnterContract.View) {
        super.attach(view)

        when (onboardingInteractor.currentFlow) {
            is OnboardingFlow.CreateWallet -> view.initCreateWalletViews()
            is OnboardingFlow.RestoreWallet -> view.initRestoreWalletViews()
        }

        launch { loadDefaultCountryCode() }
    }

    private suspend fun loadDefaultCountryCode() {
        try {
            val countryCode: CountryCode? =
                countryCodeInteractor.detectCountryCodeBySimCard()
                    ?: countryCodeInteractor.detectCountryCodeByNetwork()
                    ?: countryCodeInteractor.detectCountryCodeByLocale()

            selectedCountryCode = countryCode

            view?.showDefaultCountryCode(countryCode)
        } catch (e: Throwable) {
            Timber.e(e, "Loading default country code failed")
            view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
        }
    }

    override fun onCountryCodeChanged(newCountryCode: String) {
        launch {
            selectedCountryCode = countryCodeInteractor.findCountryForPhoneCode(newCountryCode)
            view?.update(selectedCountryCode)
        }
    }

    override fun onPhoneChanged(phoneNumber: String) {
        selectedCountryCode?.let {
            val isValidNumber = countryCodeInteractor.isValidNumberForRegion(it.phoneCode, phoneNumber)
            val newButtonState = if (isValidNumber) {
                PhoneNumberScreenContinueButtonState.ENABLED_TO_CONTINUE
            } else {
                PhoneNumberScreenContinueButtonState.DISABLED_INPUT_IS_EMPTY
            }
            view?.setContinueButtonState(newButtonState)
        }
    }

    override fun onCountryCodeChanged(newCountry: CountryCode) {
        selectedCountryCode = newCountry
        view?.update(selectedCountryCode)
    }

    override fun onCountryCodeInputClicked() {
        view?.showCountryCodePicker(selectedCountryCode)
    }

    override fun submitUserPhoneNumber(phoneNumber: String) {
        launch {
            when (onboardingInteractor.currentFlow) {
                is OnboardingFlow.CreateWallet -> startCreatingWallet(phoneNumber)
                is OnboardingFlow.RestoreWallet -> startRestoringCustomShare(phoneNumber)
            }
        }
    }

    private suspend fun startCreatingWallet(phoneNumber: String) {
        try {
            selectedCountryCode?.let {
                if (createWalletInteractor.getUserEnterPhoneNumberTriesCount() >= MAX_PHONE_NUMBER_TRIES) {
                    view?.navigateToAccountBlocked()
                } else {
                    val userPhoneNumber = PhoneNumber(it.phoneCode + phoneNumber)
                    createWalletInteractor.startCreatingWallet(userPhoneNumber = userPhoneNumber)
                    view?.navigateToSmsInput()
                }
            }
        } catch (error: Throwable) {
            if (error is GatewayServiceError) {
                handleGatewayServiceError(error)
            } else {
                Timber.e(error, "Phone number submission failed")
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            }
            createWalletInteractor.setIsCreateWalletRequestSent(isSent = false)
        }
    }

    private fun handleGatewayServiceError(gatewayServiceError: GatewayServiceError) {
        Timber.i(gatewayServiceError)
        when (gatewayServiceError) {
            is GatewayServiceError.TooManyOtpRequests -> {
                Timber.e(gatewayServiceError)
                view?.showUiKitSnackBar(messageResId = R.string.error_too_often_otp_requests_message)
            }
            is GatewayServiceError.PhoneNumberNotExists -> {
                Timber.e(gatewayServiceError)
                val isDeviceShareSaved = restoreWalletInteractor.isDeviceShareSaved()

                val title = if (isDeviceShareSaved) {
                    resourcesProvider.getString(R.string.restore_no_wallet_title)
                } else {
                    resourcesProvider.getString(R.string.restore_no_account_title)
                }
                val message = if (isDeviceShareSaved) {
                    val userEmailAddress = restoreWalletInteractor.getUserEmailAddress()
                    resourcesProvider.getString(
                        R.string.restore_no_wallet_found_with_device_share_message,
                        userEmailAddress.orEmpty()
                    )
                } else {
                    val userPhoneNumber = restoreWalletInteractor.getUserPhoneNumber()?.formattedValue.orEmpty()
                    resourcesProvider.getString(
                        R.string.restore_no_wallet_found_with_no_device_share_message,
                        userPhoneNumber
                    )
                }
                val error = GeneralErrorScreenError.AccountNotFound(
                    isDeviceShareExists = isDeviceShareSaved,
                    title = title,
                    message = message
                )
                view?.navigateToCriticalErrorScreen(error)
            }
            is GatewayServiceError.SmsDeliverFailed -> {
                view?.showUiKitSnackBar(messageResId = R.string.onboarding_phone_enter_error_sms_failed)
            }
            is GatewayServiceError.UserAlreadyExists, is GatewayServiceError.PhoneNumberAlreadyConfirmed -> {
                view?.showUiKitSnackBar(messageResId = R.string.onboarding_phone_enter_error_phone_confirmed)
            }
            is GatewayServiceError.TooManyRequests -> {
                view?.navigateToAccountBlocked()
            }
            is GatewayServiceError.CriticalServiceFailure -> {
                Timber.e(gatewayServiceError, "Phone number submission failed with critical error")
                view?.navigateToCriticalErrorScreen(GeneralErrorScreenError.CriticalError(gatewayServiceError.code))
            }
            else -> {
                Timber.e(gatewayServiceError, "Phone number submission failed")
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            }
        }
    }

    private suspend fun startRestoringCustomShare(phoneNumber: String) {
        try {
            selectedCountryCode?.let {
                val userPhoneNumber = PhoneNumber(it.phoneCode + phoneNumber)
                restoreWalletInteractor.startRestoreCustomShare(userPhoneNumber = userPhoneNumber)
                view?.navigateToSmsInput()
            }
        } catch (error: Throwable) {
            if (error is GatewayServiceError) {
                handleGatewayServiceError(error)
            } else {
                Timber.e(error, "Phone number submission failed")
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            }
            restoreWalletInteractor.setIsRestoreWalletRequestSent(isSent = false)
        }
    }
}
