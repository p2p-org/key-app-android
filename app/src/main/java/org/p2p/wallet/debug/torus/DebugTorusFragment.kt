package org.p2p.wallet.debug.torus

import android.os.Bundle
import android.view.View
import kotlinx.coroutines.runBlocking
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = runBlocking {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }

            networkServicesUrlProvider.loadTorusEnvironment().apply {
                environmentTextView.text = baseUrl
                verifierTextView.text = buildString {
                    append("verifier = $verifier")
                    append("\n")
                    append("subVerifier = ${subVerifier.takeIf { !it.isNullOrBlank() } ?: "Not applied"}")
                }
            }

            confirmButton.setOnClickListener {
                val newUrl = environmentEditText.text.toString()
                val newVerifier = verifierEditText.text.toString()
                val newSubVerifier = subVerifierEditText.text.toString()
                updateEnvironmentAndRestart(
                    newUrl = newUrl,
                    newVerifier = newVerifier,
                    newSubVerifier = newSubVerifier
                )
            }

            val signUpDetails = signUpDetailsStorage.getLastSignUpUserDetails()
            val hasShare = signUpDetails?.signUpDetails?.deviceShare != null
            shareTextView.text = if (hasShare) {
                signUpDetails?.signUpDetails?.deviceShare.toString()
            } else {
                "No device share"
            }
            shareDeleteButton.also {
                it.isEnabled = hasShare
                it.setOnClickListener {
                    //TODO create new solution
                    //signUpDetailsStorage.removeAllShares()
                    appRestarter.restartApp()
                }
            }
        }
    }

    private fun updateEnvironmentAndRestart(
        newUrl: String? = null,
        newVerifier: String? = null,
        newSubVerifier: String? = null
    ) {
        networkServicesUrlProvider.saveTorusEnvironment(
            newUrl = newUrl?.ifEmpty { null },
            newVerifier = newVerifier?.ifEmpty { null },
            newSubVerifier = newSubVerifier
        )
        appRestarter.restartApp()
    }
}
