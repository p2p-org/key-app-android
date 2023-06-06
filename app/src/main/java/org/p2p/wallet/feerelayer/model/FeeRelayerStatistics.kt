package org.p2p.wallet.feerelayer.model

import org.p2p.solanaj.core.OperationType
import org.p2p.wallet.BuildConfig
import org.p2p.core.utils.Constants
import org.p2p.wallet.feerelayer.model.FeeRelayerEnvironment.DEVELOP
import org.p2p.wallet.feerelayer.model.FeeRelayerEnvironment.RELEASE

class FeeRelayerStatistics(
    val operationType: OperationType,
    val currency: String,
    val environment: FeeRelayerEnvironment = if (BuildConfig.DEBUG) DEVELOP else RELEASE,
    val deviceType: String = Constants.DEVICE_TYPE,
    val build: String = BuildConfig.VERSION_NAME
)


enum class FeeRelayerEnvironment(val stringValue: String) {
    DEVELOP("dev"),
    RELEASE("release");
}
