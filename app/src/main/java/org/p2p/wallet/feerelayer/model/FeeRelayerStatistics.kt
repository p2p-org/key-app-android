package org.p2p.wallet.feerelayer.model

import org.p2p.solanaj.core.OperationType
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.utils.Constants

class FeeRelayerStatistics(
    val operationType: OperationType,
    val currency: String,
    val deviceType: String = Constants.DEVICE_TYPE,
    val build: String = BuildConfig.VERSION_NAME
)
