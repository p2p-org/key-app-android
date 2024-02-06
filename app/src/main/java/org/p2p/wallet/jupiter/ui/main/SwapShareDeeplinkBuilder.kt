package org.p2p.wallet.jupiter.ui.main

import android.net.Uri
import org.p2p.core.crypto.Base58String
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.common.feature_toggles.toggles.remote.ReferralProgramEnabledFeatureToggle
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider

class SwapShareDeeplinkBuilder(
    private val tokenKeyProvider: TokenKeyProvider,
    private val usernameInteractor: UsernameInteractor,
    private val referralProgramEnabledFt: ReferralProgramEnabledFeatureToggle
) {
    fun buildDeeplink(tokenAMint: Base58String, tokenBMint: Base58String): String {
        val userReferralId = usernameInteractor.getUsername()?.fullUsername ?: tokenKeyProvider.publicKey

        return Uri.parse("https://s.key.app/swap").buildUpon()
            .appendQueryParameter("from", tokenAMint.base58Value)
            .appendQueryParameter("to", tokenBMint.base58Value)
            .apply {
                if (referralProgramEnabledFt.isFeatureEnabled) {
                    appendQueryParameter("r", userReferralId)
                }
            }
            .build()
            .toString()
    }
}
