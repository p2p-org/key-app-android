package com.p2p.wowlet.fragment.pincode.view

import android.content.Intent
import android.util.Log

import android.view.View
import android.widget.Toast
import com.p2p.wowlet.R
import com.p2p.wowlet.activity.MainActivity
import com.p2p.wowlet.appbase.FragmentBaseMVVM
import com.p2p.wowlet.appbase.utils.dataBinding
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewcommand.ViewCommand
import com.p2p.wowlet.databinding.FragmentPinCodeBinding
import com.p2p.wowlet.fragment.pincode.adapter.PinButtonAdapter
import com.p2p.wowlet.fragment.pincode.viewmodel.PinCodeViewModel
import com.p2p.wowlet.utils.isFingerPrintSet
import kotlinx.android.synthetic.main.fragment_pin_code.*
import com.p2p.wowlet.utils.openFingerprintDialog
import com.wowlet.entities.enums.PinCodeFragmentType
import org.koin.androidx.viewmodel.ext.android.viewModel

class PinCodeFragment : FragmentBaseMVVM<PinCodeViewModel, FragmentPinCodeBinding>() {
    override val viewModel: PinCodeViewModel by viewModel()
    override val binding: FragmentPinCodeBinding by dataBinding(R.layout.fragment_pin_code)

    companion object {
        const val OPEN_FRAGMENT_SPLASH_SCREEN = "openFragmentSplashScreen"
        const val CREATE_NEW_PIN_CODE = "createNewPinCode"
    }

    private var isSplashScreen: Boolean = false
    private lateinit var pinCodeFragmentType: PinCodeFragmentType


    override fun initView() {
        binding.run {
            viewModel = this@PinCodeFragment.viewModel

            Log.i("FingerPring", "initView: " + requireActivity().isFingerPrintSet())
            pinView.pinCodeFragmentType = pinCodeFragmentType
            pinView.setMaxPinSize(6)
            context?.let { context ->
                gridView.adapter = PinButtonAdapter(
                    context,
                    pinCodeFragmentType,
                    pinButtonClick = {
                        pinView.onPinButtonClicked(text = it)
                        vPinCodeNotMatch.visibility = View.INVISIBLE
                    },
                    pinFingerPrint = {
                        activity?.openFingerprintDialog {
                            this@PinCodeFragment.viewModel.goToFingerPrintFragment()
                        }
                    },
                    removeCode = {
                        pinView.onDeleteButtonClicked()
                        vPinCodeNotMatch.visibility = View.INVISIBLE
                    })
            }
            gridView.numColumns = 3
        }
        initPinCodeMassage()
    }

    override fun initData() {
        arguments?.let {
            isSplashScreen = it.getBoolean(OPEN_FRAGMENT_SPLASH_SCREEN, false)
            pinCodeFragmentType = it.get(CREATE_NEW_PIN_CODE) as PinCodeFragmentType
        }
    }

    private fun initPinCodeMassage() {
        if (isSplashScreen)
            vPinCodeMessage.text = getString(R.string.enter_the_code)
        else
            vPinCodeMessage.text = getString(R.string.create_a_pin_code_info)
    }


    override fun observes() {
        observe(viewModel.pinCodeSuccess) {
            viewModel.fingerPrintStatus()
        }
        observe(viewModel.pinCodeSaved) {
            vPinCodeMessage.text = getString(R.string.confirm_pin_code)
            pinView.clearPin()
            pinView.isFirstPinInput = true
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
            when (pinCodeFragmentType) {
                PinCodeFragmentType.CREATE -> {
                    vPinCodeNotMatch.text = getString(R.string.pin_codes_invalid)
                    //vPinCodeMessage.text = getString(R.string.create_a_pin_code_info)
                    pinView.isFirstPinInput = true
                    pinView.clearPin()
                }
                PinCodeFragmentType.VERIFY -> {
                    pinView.errorPinViewsDesign()
                    if(pinCodeFragmentType==PinCodeFragmentType.VERIFY)
                        binding.resetPinCode.visibility=View.VISIBLE
                    else
                        binding.resetPinCode.visibility=View.GONE
                    when (pinView.wrongPinCodeCount) {
                        1 -> vPinCodeNotMatch.text = getString(R.string.wrong_pin_code_left_2)
                        2 -> vPinCodeNotMatch.text = getString(R.string.wrong_pin_code_left_1)
                        else -> vPinCodeNotMatch.text = getString(R.string.wrong_pin_code_block)
                    }
                }
            }
        }

        pinView.createPinCode = {
            viewModel.initCode(it)
        }
        pinView.verifyPinCode = {
            viewModel.verifyPinCode(it)
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
        else {
            if (pinCodeFragmentType == PinCodeFragmentType.CREATE && pinView.isFirstPinInput) {
                vPinCodeMessage.text = getString(R.string.create_a_pin_code_info)
                binding.vPinCodeNotMatch.visibility = View.GONE
                binding.pinView.isFirstPinInput = false
            } else {
                viewModel.navigateUp()
            }
        }
    }
}