package com.p2p.wowlet.fragment.faceid.view

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.p2p.wowlet.R
import com.p2p.wowlet.activity.MainActivity
import com.p2p.wowlet.appbase.FragmentBaseMVVM
import com.p2p.wowlet.appbase.utils.dataBinding
import com.p2p.wowlet.appbase.viewcommand.Command.NavigateNotificationViewCommand
import com.p2p.wowlet.appbase.viewcommand.Command.NavigateUpViewCommand
import com.p2p.wowlet.appbase.viewcommand.ViewCommand
import com.p2p.wowlet.databinding.FragmentFaceIdBinding
import com.p2p.wowlet.fragment.faceid.viewmodel.FaceIdViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.Executor

class FaceIdFragment : FragmentBaseMVVM<FaceIdViewModel, FragmentFaceIdBinding>() {

    override val viewModel: FaceIdViewModel by viewModel()
    override val binding: FragmentFaceIdBinding by dataBinding(R.layout.fragment_face_id)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            viewModel = this@FaceIdFragment.viewModel
        }
        binding.btUseFaceID.setOnClickListener {
            openFingerprintDialog()
        }
    }

    private fun openFingerprintDialog() {
        val executor = ContextCompat.getMainExecutor(requireContext())
        val biometricManager = BiometricManager.from(requireContext())
        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                authUser(executor)
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_msg_no_biometric_hardware),
                    Toast.LENGTH_LONG
                ).show()
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_msg_biometric_hw_unavailable),
                    Toast.LENGTH_LONG
                ).show()
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                when {
                    android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R -> {
                        startActivity(Intent(Settings.ACTION_BIOMETRIC_ENROLL))
                    }
                    else -> {
                        startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                    }
                }
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_msg_biometric_not_setup),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun authUser(executor: Executor) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.auth_title))
            .setSubtitle(getString(R.string.auth_subtitle))
            .setDescription(getString(R.string.auth_description))
            .setDeviceCredentialAllowed(true)
            .build()

        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    viewModel.goToNotificationFragment()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(requireContext(), "Auth error", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(requireContext(), "Auth failed", Toast.LENGTH_SHORT).show()
                }
            })

        biometricPrompt.authenticate(promptInfo)
    }

    override fun processViewCommand(command: ViewCommand) {
        when (command) {
            is NavigateUpViewCommand -> {
                navigateFragment(command.destinationId)
            }
            is NavigateNotificationViewCommand -> {
                navigateFragment(command.destinationId)
            }
        }
    }

    override fun navigateUp() {
        viewModel.navigateUp()
    }
}