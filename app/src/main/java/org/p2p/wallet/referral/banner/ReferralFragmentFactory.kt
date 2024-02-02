package org.p2p.wallet.referral.banner

import androidx.fragment.app.Fragment
import android.content.Context
import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.referral.ReferralFragment
import org.p2p.wallet.utils.shareText

class ReferralFragmentFactory(
    private val tokenKeyProvider: TokenKeyProvider,
    private val usernameInteractor: UsernameInteractor
) {
    fun shareLink(context: Context) {
        val userReferralId = usernameInteractor.getUsername()?.fullUsername ?: tokenKeyProvider.publicKey
        val sharingLink = context.getString(R.string.share_referral_link, userReferralId)
        context.shareText(sharingLink)
    }

    fun openDetails(): Fragment {
        return ReferralFragment.create()
    }
}
