package org.p2p.wallet.debug.torus

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.repository.UserSignUpDetailsStorage
import org.p2p.wallet.common.AppRestarter
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentDebugTorusBinding
import org.p2p.wallet.infrastructure.network.environment.NetworkServicesUrlProvider
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding

class DebugTorusFragment : BaseFragment(R.layout.fragment_debug_torus) {

    companion object {
        fun create(): DebugTorusFragment = DebugTorusFragment()
    }

    private val binding: FragmentDebugTorusBinding by viewBinding()

    private val networkServicesUrlProvider: NetworkServicesUrlProvider by inject()
    private val signUpDetailsStorage: UserSignUpDetailsStorage by inject()

    private val appRestarter: AppRestarter by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }

            networkServicesUrlProvider.loadTorusEnvironment().apply {
                environmentTextView.text = baseUrl
                verifierTextView.text = verifier
            }

            testUrlButton.setOnClickListener {
                val testUrl = getString(R.string.torusBaseUrl)
                updateEnvironmentAndRestart(testUrl)
            }

            releaseUrlButton.setOnClickListener {
                val releaseUrl = getString(R.string.torusTestUrl)
                updateEnvironmentAndRestart(releaseUrl)
            }

            testVerifierButton.setOnClickListener {
                val testVerifier = getString(R.string.torusDebugVerifier)
                updateEnvironmentAndRestart(newVerifier = testVerifier)
            }

            releaseVerifierButton.setOnClickListener {
                val releaseVerifier = getString(R.string.torusFeatureVerifier)
                updateEnvironmentAndRestart(newVerifier = releaseVerifier)
            }

            confirmButton.setOnClickListener {
                val newUrl = environmentEditText.text.toString()
                val newVerifier = verifierEditText.text.toString()
                updateEnvironmentAndRestart(newUrl = newUrl, newVerifier = newVerifier)
            }

            val signUpDetails = signUpDetailsStorage.getLastSignUpUserDetails()
            val hasShare = signUpDetails?.signUpDetails?.deviceShare != null
            shareTextView.text = if (hasShare) {
                signUpDetails?.signUpDetails?.deviceShare.toString()
            } else {
                "No device share"
            }
            shareDeleteButton.apply {
                isEnabled = hasShare
                setOnClickListener {
                    signUpDetailsStorage.removeLastDeviceShare()
                    appRestarter.restartApp()
                }
            }
        }
    }

    private fun updateEnvironmentAndRestart(newUrl: String? = null, newVerifier: String? = null) {
        networkServicesUrlProvider.saveTorusEnvironment(
            newUrl = newUrl?.ifEmpty { null },
            newVerifier = newVerifier?.ifEmpty { null }
        )
        appRestarter.restartApp()
    }
}
