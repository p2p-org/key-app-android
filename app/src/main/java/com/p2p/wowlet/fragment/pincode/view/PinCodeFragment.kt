package com.p2p.wowlet.fragment.pincode.view

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.p2p.wowlet.R
import com.p2p.wowlet.activity.MainActivity
import com.p2p.wowlet.appbase.FragmentBaseMVVM
import com.p2p.wowlet.appbase.utils.dataBinding
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewcommand.ViewCommand
import com.p2p.wowlet.databinding.FragmentPinCodeBinding
import com.p2p.wowlet.fragment.pincode.viewmodel.PinCodeViewModel
import kotlinx.android.synthetic.main.fragment_pin_code.*
import com.p2p.wowlet.supportclass.simplepinlock.PinButtonAdapter
import com.p2p.wowlet.utils.openFingerprintDialog
import org.koin.androidx.viewmodel.ext.android.viewModel


class PinCodeFragment : FragmentBaseMVVM<PinCodeViewModel, FragmentPinCodeBinding>() {
    override val viewModel: PinCodeViewModel by viewModel()
    override val binding: FragmentPinCodeBinding by dataBinding(R.layout.fragment_pin_code)

    companion object {
        const val OPEN_FRAGMENT_SPLASH_SCREEN = "openFragmentSplashScreen"
        const val CREATE_NEW_PIN_CODE = "createNewPinCode"
    }

    private var isSplashScreen: Boolean = false
    private var createNewPinCode: Boolean = false
    private var pin = ""
    private var isFirstPinInput = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.run {
            viewModel = this@PinCodeFragment.viewModel
        }

        gridView.adapter = PinButtonAdapter(
            context!!,
            pinButtonClick = { onPinButtonClicked(text = it) },
            pinFingerPrint = {
                activity?.openFingerprintDialog() {
                    viewModel.goToFingerPrintFragment()
                }
            },
            pinReset = {
                initCreateNewPinCode()
            })
        gridView.numColumns = 3
        reloadPinView()
        initPinCodeMassage()

    }

    override fun initData() {
        arguments?.let {
            isSplashScreen = it.getBoolean(OPEN_FRAGMENT_SPLASH_SCREEN, false)
            createNewPinCode = it.getBoolean(CREATE_NEW_PIN_CODE, false)
        }
    }

    private fun getMaxPinSize(): Int {
        return 6
    }

    private fun onPinButtonClicked(text: String) {
        if (pin.length < getMaxPinSize()) {
            if (createNewPinCode && !isFirstPinInput) {
                vPinCodeMessage.text = getString(R.string.create_a_pin_code_info)
            }
            vPinCodeNotMatch.visibility = View.INVISIBLE
            this.pin += text
            reloadPinView()
        }
        if (pin.length == getMaxPinSize()) {
            if (createNewPinCode) {
                if (isFirstPinInput)
                    this@PinCodeFragment.viewModel.verifyPinCode(pin)
                else
                    this@PinCodeFragment.viewModel.initCode(pin)
            } else {
                this@PinCodeFragment.viewModel.verifyPinCode(pin)
            }
        }
    }

    private fun initPinCodeMassage() {
        if (isSplashScreen)
            vPinCodeMessage.text = getString(R.string.enter_the_code)
        else
            vPinCodeMessage.text = getString(R.string.create_a_pin_code_info)
    }

    private fun initCreateNewPinCode() {
        viewModel.goToSecretKeyFragment()
    }

    private fun clearPin() {
        pin = ""
        reloadPinView()
    }

    private fun reloadPinView() {
        val dotSize = resources.getDimensionPixelSize(R.dimen.dp_20)
        val dotMargin = resources.getDimensionPixelSize(R.dimen.dp_10)
        pinView.removeAllViews()
        (1..getMaxPinSize()).forEach {
            val imageView = ImageView(context)
            val layoutParams = LinearLayout.LayoutParams(dotSize, dotSize, 0.0f)
            layoutParams.setMargins(dotMargin, dotMargin, dotMargin, dotMargin)
            imageView.layoutParams = layoutParams
            if (it > pin.length) {
                imageView.setImageResource(R.drawable.bg_pin_code_dot_empty)
            } else {
                imageView.setImageResource(R.drawable.bg_pin_code_dot_fill)
            }
            pinView.addView(imageView)
        }
    }

    override fun observes() {
        observe(viewModel.pinCodeSuccess) {
            viewModel.fingerPrintStatus()
        }
        observe(viewModel.pinCodeSaved) {
            vPinCodeMessage.text = getString(R.string.confirm_pin_code)
            clearPin()
            isFirstPinInput = true
        }
        observe(viewModel.pinCodeError) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
        observe(viewModel.isSkipFingerPrint) {
            if (it)
                viewModel.notificationStatus()
            else
                viewModel.goToFingerPrintFragment()
        }
        observe(viewModel.skipNotification) {
            if (it)
                viewModel.goToRegFinishFragment()
            else
                viewModel.goToNotificationFragment()
        }
        observe(viewModel.verifyPinCodeError) {
            pin = ""
            if (createNewPinCode) {
                vPinCodeNotMatch.text = getString(R.string.pin_codes_invalid)
                isFirstPinInput = false
            } else {
                vPinCodeNotMatch.text = getString(R.string.pin_codes_doesn_s_match)
            }
        }
    }

    override fun processViewCommand(command: ViewCommand) {
        when (command) {
            is Command.NavigateUpViewCommand -> navigateFragment(command.destinationId)
            is Command.NavigateSecretKeyViewCommand -> navigateFragment(command.destinationId)
            is Command.NavigateFingerPrintViewCommand -> navigateFragment(command.destinationId)
            is Command.NavigateNotificationViewCommand -> navigateFragment(command.destinationId)
            is Command.NavigateRegLoginViewCommand -> navigateFragment(command.destinationId)
            is Command.NavigateRegFinishViewCommand -> navigateFragment(command.destinationId)
            is Command.OpenMainActivityViewCommand -> {
                activity?.let {
                    val intent = Intent(it, MainActivity::class.java)
                    it.startActivity(intent)
                    it.finish()
                }
            }
        }
    }

    override fun navigateUp() {
        if (isSplashScreen)
            viewModel.finishApp()
        else
            viewModel.navigateUp()
    }
}