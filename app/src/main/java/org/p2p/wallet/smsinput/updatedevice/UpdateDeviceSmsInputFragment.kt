package org.p2p.wallet.smsinput.updatedevice

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.GatewayHandledState
import org.p2p.wallet.auth.model.PhoneNumber
import org.p2p.wallet.auth.model.RestoreFailureState
import org.p2p.wallet.auth.ui.generalerror.timer.OnboardingGeneralErrorTimerFragment
import org.p2p.wallet.common.NavigationStrategy
import org.p2p.wallet.home.ui.container.MainContainerFragment
import org.p2p.wallet.smsinput.BaseSmsInputFragment
import org.p2p.wallet.smsinput.SmsInputContract
import org.p2p.wallet.smsinput.SmsInputFactory
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment

class UpdateDeviceSmsInputFragment : BaseSmsInputFragment() {

    override val presenter: SmsInputContract.Presenter by inject(named(SmsInputFactory.Type.UpdateDevice.name))

    override fun onBackPressed() {
        super.onBackPressed()
        popBackStack()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.uiKitToolbar.setTitle(R.string.devices_change_sms_input_toolbar_title)
    }

    override fun initView(userPhoneNumber: PhoneNumber) {
        binding.textViewDescription.text =
            getString(R.string.onboarding_sms_input_phone_number_title, userPhoneNumber.formattedValue)
    }

    override fun navigateToSmsInputBlocked(timerLeftTime: Long) {
        replaceFragment(
            OnboardingGeneralErrorTimerFragment.create(
                timerLeftTime = timerLeftTime,
                navigationStrategy = NavigationStrategy.PopBackStackTo(MainContainerFragment::class.java)
            )
        )
    }

    override fun navigateToGatewayErrorScreen(handledState: GatewayHandledState) {
        showUiKitSnackBar(messageResId = R.string.error_general_message)
    }

    override fun navigateToRestoreErrorScreen(handledState: RestoreFailureState.TitleSubtitleError) = Unit

    override fun navigateToExceededDailyResendSmsLimit() = Unit

    override fun navigateToExceededConfirmationAttempts() = Unit
}
