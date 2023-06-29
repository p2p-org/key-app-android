package org.p2p.wallet.alarmlogger.model

data class StrigaAlarmError(
    val source: String,
    val kycSdkState: String,
    val error: String
)
