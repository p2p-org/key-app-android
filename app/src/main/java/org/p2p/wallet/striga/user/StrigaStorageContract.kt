package org.p2p.wallet.striga.user

import org.p2p.wallet.striga.user.model.StrigaUserStatusDetails

interface StrigaStorageContract {
    var userStatus: StrigaUserStatusDetails?
    var signupSmsExceededVerificationAttemptsMillis: Long
    var signupSmsExceededResendAttemptsMillis: Long
    var onrampSmsExceededVerificationAttemptsMillis: Long
    var onrampSmsExceededResendAttemptsMillis: Long

    fun clear() {
        userStatus = null
        signupSmsExceededVerificationAttemptsMillis = 0
        signupSmsExceededResendAttemptsMillis = 0
        onrampSmsExceededVerificationAttemptsMillis = 0
        onrampSmsExceededResendAttemptsMillis = 0
    }
}
