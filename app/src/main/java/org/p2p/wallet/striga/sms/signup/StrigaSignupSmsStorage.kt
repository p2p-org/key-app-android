package org.p2p.wallet.striga.sms.signup

import org.p2p.wallet.striga.sms.StrigaSmsStorageContract
import org.p2p.wallet.striga.user.StrigaStorageContract

class StrigaSignupSmsStorage(
    private val storage: StrigaStorageContract
) : StrigaSmsStorageContract {

    override var exceededVerificationAttemptsMillis: Long
        get() = storage.signupSmsExceededVerificationAttemptsMillis
        set(value) {
            storage.signupSmsExceededVerificationAttemptsMillis = value
        }

    override var exceededResendAttemptsMillis: Long
        get() = storage.signupSmsExceededResendAttemptsMillis
        set(value) {
            storage.signupSmsExceededResendAttemptsMillis = value
        }
}
