package org.p2p.wallet.striga.sms.onramp

import org.p2p.wallet.striga.sms.StrigaSmsStorageContract
import org.p2p.wallet.striga.user.StrigaStorageContract

class StrigaOnRampSmsStorage(
    private val storage: StrigaStorageContract
) : StrigaSmsStorageContract {

    override var exceededVerificationAttemptsMillis: Long
        get() = storage.onrampSmsExceededVerificationAttemptsMillis
        set(value) {
            storage.onrampSmsExceededVerificationAttemptsMillis = value
        }

    override var exceededResendAttemptsMillis: Long
        get() = storage.onrampSmsExceededResendAttemptsMillis
        set(value) {
            storage.onrampSmsExceededResendAttemptsMillis = value
        }
}
