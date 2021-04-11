package com.p2p.wowlet.auth.ui.pincode.view

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isInvisible
import com.p2p.wowlet.R
import com.p2p.wowlet.auth.ui.RegFinishFragment
import com.p2p.wowlet.auth.ui.RegLoginFragment
import com.p2p.wowlet.auth.ui.fingerprint.view.FingerPrintFragment
import com.p2p.wowlet.auth.ui.pincode.adapter.PinButtonAdapter
import com.p2p.wowlet.auth.ui.pincode.viewmodel.PinCodeViewModel
import com.p2p.wowlet.backupwallat.secretkeys.view.SecretKeyFragment
import com.p2p.wowlet.common.mvp.BaseFragment
import com.p2p.wowlet.dashboard.view.DashboardFragment
import com.p2p.wowlet.databinding.FragmentPinCodeBinding
import com.p2p.wowlet.deprecated.viewcommand.Command
import com.p2p.wowlet.deprecated.viewcommand.ViewCommand
import com.p2p.wowlet.entities.enums.PinCodeFragmentType
import com.p2p.wowlet.home.HomeFragment
import com.p2p.wowlet.notification.view.NotificationFragment
import com.p2p.wowlet.utils.args
import com.p2p.wowlet.utils.isFingerPrintSet
import com.p2p.wowlet.utils.openFingerprintDialog
import com.p2p.wowlet.utils.popAndReplaceFragment
import com.p2p.wowlet.utils.replaceFragment
import com.p2p.wowlet.utils.viewbinding.viewBinding
import com.p2p.wowlet.utils.withArgs
import kotlinx.android.synthetic.main.fragment_pin_code.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class PinCodeFragment : BaseFragment(R.layout.fragment_pin_code) {
    private val viewModel: PinCodeViewModel by viewModel()
    private val binding: FragmentPinCodeBinding by viewBinding()

    companion object {
        const val OPEN_FRAGMENT_SPLASH_SCREEN = "openFragmentSplashScreen"
        const val OPEN_FRAGMENT_BACKUP_DIALOG = "openFragmentBackupDialog"
        const val PIN_TYPE = "PIN_TYPE"

        fun create(
            openSplashScreen: Boolean,
            isBackupDialog: Boolean,
            type: PinCodeFragmentType
        ): PinCodeFragment =
            PinCodeFragment().withArgs(
                OPEN_FRAGMENT_SPLASH_SCREEN to openSplashScreen,
                OPEN_FRAGMENT_BACKUP_DIALOG to isBackupDialog,
                PIN_TYPE to type
            )
    }

    private var isSplashScreen: Boolean = false
    private var isBackupDialog: Boolean = false
    private var isFingerprintEnabled: Boolean = false
    private val pinCodeFragmentType: PinCodeFragmentType by args(PIN_TYPE)
    private lateinit var pinButtonAdapter: PinButtonAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initData()
        observeData()
    }

    private fun initView() {
        viewModel.fingerPrintStatus()
        binding.run {
            Log.i("FingerPring", "initView: " + requireActivity().isFingerPrintSet())
            pinView.pinCodeFragmentType = pinCodeFragmentType
            pinView.setMaxPinSize(6)
            context?.let { context ->
                pinButtonAdapter = PinButtonAdapter(
                    isFingerprintEnabled,
                    context,
                    pinCodeFragmentType,
                    pinButtonClick = {
                        pinView.onPinButtonClicked(text = it)
                        vPinCodeNotMatch.visibility = View.INVISIBLE
                    },
                    pinFingerPrint = {
                        this@PinCodeFragment.viewModel.openFingerprintDialog()
                    },
                    removeCode = {
                        pinView.onDeleteButtonClicked()
                        vPinCodeNotMatch.visibility = View.INVISIBLE
                    }
                )
                gridView.adapter = pinButtonAdapter
            }
            gridView.numColumns = 3

            resetPinCode.setOnClickListener {
                replaceFragment(SecretKeyFragment())
            }
        }
        initPinCodeMassage()
    }

    private fun initData() {
        arguments?.let {
            isSplashScreen = it.getBoolean(OPEN_FRAGMENT_SPLASH_SCREEN, false)
            isBackupDialog = it.getBoolean(OPEN_FRAGMENT_BACKUP_DIALOG, false)
        }
    }

    private fun initPinCodeMassage() {
        if (isSplashScreen || isBackupDialog)
            vPinCodeMessage.text = getString(R.string.enter_the_code)
        else
            vPinCodeMessage.text = getString(R.string.create_a_pin_code_info)
    }

    private fun observeData() {
        viewModel.pinCodeSuccess.observe(viewLifecycleOwner) {
            if (isFingerprintEnabled) {
                if (isBackupDialog)
                    replaceFragment(DashboardFragment.create(true))
                else
                    viewModel.notificationStatus()
            } else {
                if (pinCodeFragmentType == PinCodeFragmentType.CREATE) {
                    replaceFragment(FingerPrintFragment())
                } else {
                    if (isBackupDialog)
                        replaceFragment(DashboardFragment.create(true))
                    else
                        viewModel.notificationStatus()
                }
            }
        }
        viewModel.pinCodeSaved.observe(viewLifecycleOwner) {
            vPinCodeMessage.text = getString(R.string.confirm_pin_code)
            binding.pinView.clearPin()
            binding.pinView.isFirstPinInput = true
        }
        viewModel.pinCodeError.observe(viewLifecycleOwner) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
        viewModel.isSkipFingerPrint.observe(viewLifecycleOwner) {
            isFingerprintEnabled = it
            pinButtonAdapter.updateFingerprintStatus(isFingerprintEnabled)
            if (it) {
                childFragmentManager.executePendingTransactions()
                viewModel.openFingerprintDialog()
            }
        }
        viewModel.skipNotification.observe(viewLifecycleOwner) {
            if (it) {
                replaceFragment(RegFinishFragment())
            } else {
                replaceFragment(NotificationFragment())
            }
        }

        viewModel.verifyPinCodeError.observe(viewLifecycleOwner) {
            vPinCodeNotMatch.isInvisible = !it
            when (pinCodeFragmentType) {
                PinCodeFragmentType.CREATE -> {
                    vPinCodeNotMatch.text = getString(R.string.pin_codes_invalid)
                    // vPinCodeMessage.text = getString(R.string.create_a_pin_code_info)
                    binding.pinView.isFirstPinInput = true
                    binding.pinView.clearPin()
                }
                PinCodeFragmentType.VERIFY -> {
                    binding.pinView.errorPinViewsDesign()
                    if (pinCodeFragmentType == PinCodeFragmentType.VERIFY)
                        binding.resetPinCode.visibility = View.VISIBLE
                    else
                        binding.resetPinCode.visibility = View.GONE
                    if (isBackupDialog)
                        vPinCodeNotMatch.text = getString(R.string.incorrect_password)
                    else
                        when (binding.pinView.wrongPinCodeCount) {
                            1 ->
                                vPinCodeNotMatch.text =
                                    getString(R.string.wrong_pin_code_left_2)
                            2 ->
                                vPinCodeNotMatch.text =
                                    getString(R.string.wrong_pin_code_left_1)
                            else ->
                                vPinCodeNotMatch.text =
                                    getString(R.string.wrong_pin_code_block)
                        }
                }
            }
        }
        viewModel.openFingerprintDialog.observe(viewLifecycleOwner) {
            openFingerprintDialog {
                if (isBackupDialog)
                    replaceFragment(DashboardFragment.create(true))
                else
                    replaceFragment(FingerPrintFragment())
            }
        }

        viewModel.command.observe(viewLifecycleOwner) {
            processViewCommand(it)
        }

        binding.pinView.createPinCode = {
            viewModel.initCode(it)
        }
        binding.pinView.verifyPinCode = {
            viewModel.verifyPinCode(it)
        }
    }

    private fun processViewCommand(command: ViewCommand) {
        when (command) {
            is Command.NavigateSecretKeyViewCommand -> replaceFragment(SecretKeyFragment())
            is Command.NavigateFingerPrintViewCommand -> replaceFragment(FingerPrintFragment())
            is Command.NavigateNotificationViewCommand -> replaceFragment(NotificationFragment())
            is Command.NavigateRegLoginViewCommand -> replaceFragment(RegLoginFragment())
            is Command.NavigateRegFinishViewCommand -> replaceFragment(RegFinishFragment())
            is Command.OpenMainActivityViewCommand -> {
                popAndReplaceFragment(HomeFragment.create(), inclusive = true)
            }
        }
    }

//    override fun navigateUp() {
//        if (isSplashScreen)
//            requireActivity().finish()
//        else if (isBackupDialog) {
//            replaceFragment(DashboardFragment.create(false))
//        } else {
//            if (pinCodeFragmentType == PinCodeFragmentType.CREATE && binding.pinView.isFirstPinInput) {
//                vPinCodeMessage.text = getString(R.string.create_a_pin_code_info)
//                binding.vPinCodeNotMatch.visibility = View.GONE
//                binding.pinView.isFirstPinInput = false
//            } else {
//                popBackStackTo(RegLoginFragment::class)
//            }
//        }
//    }
}