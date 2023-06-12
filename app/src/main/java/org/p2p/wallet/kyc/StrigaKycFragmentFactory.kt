package org.p2p.wallet.kyc

import androidx.fragment.app.Fragment
import org.p2p.wallet.kyc.model.StrigaKycSignUpStatus

class StrigaKycFragmentFactory() {

    fun kycFragment(status: StrigaKycSignUpStatus): Fragment {
        when (status) {
            StrigaKycSignUpStatus.IDENTIFY -> TODO()
            StrigaKycSignUpStatus.PENDING -> TODO()
            StrigaKycSignUpStatus.VERIFICATION_DONE -> TODO()
            StrigaKycSignUpStatus.ACTION_REQUIRED -> TODO()
            StrigaKycSignUpStatus.REJECTED -> TODO()
        }
    }
}
