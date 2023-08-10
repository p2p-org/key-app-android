package org.p2p.wallet.alarmlogger.model

enum class DeviceShareChangeAlarmErrorSource(val sourceName: String) {
    TORUS("torus"),
    PUSH_SERVICE("push service"),
    OTHER("other")
}

data class DeviceShareChangeAlarmError(
    val source: String,
    val cause: Throwable
)
