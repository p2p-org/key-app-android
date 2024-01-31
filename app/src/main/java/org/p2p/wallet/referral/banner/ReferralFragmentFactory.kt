package org.p2p.wallet.referral.banner

import androidx.fragment.app.Fragment
import org.p2p.wallet.referral.ReferralFragment

class ReferralFragmentFactory {
    fun shareLink() {}
    fun openDetails(): Fragment {
        return ReferralFragment.create()
    }
}
