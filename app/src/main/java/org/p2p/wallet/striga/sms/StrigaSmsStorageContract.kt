package org.p2p.wallet.striga.sms

interface StrigaSmsStorageContract {
    var exceededVerificationAttemptsMillis: Long
    var exceededResendAttemptsMillis: Long
}
